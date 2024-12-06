#!/usr/bin/env bash
#
#
#
: ${HOST=localhost}
: ${PORT=8443}
: ${PROD_ID_REVS=1}
: ${PROD_ID_NOT_FOUND=12}
: ${PROD_ID_NO_REVS=213}
: ${SKIP_CB_TESTS=false}

function assertCurl(){
	local expectHttpCode=$1
	local curlCmd="$2 -w \"%{http_code}\""
	local result=$(eval $curlCmd)
	local httpCode="${result:(-3)}"
	RESPONSE='' && ((${#result} > 3)) && RESPONSE="${result%???}"

	if [ "$httpCode" = "$expectHttpCode" ]
	then
		if [ "$httpCode" = "200" ]
		then
			echo "Test OK (HTTP code= $httpCode)"
		else
			echo "Test OK (HTTP code: $httpCode, $RESPONSE)"
		fi
	else
		echo "Test FAILED, EXPECTED HTTP Code: $expectHttpCode, GOT: $httpCode, will ABORT!"
		echo "- Failing command: $curlCmd"
		echo "- Response Body: $RESPONSE"
		exit 1
	fi
}

function assertEqual() {
	local expected=$1
	local actual=$2
	local name=$3
	
	if [	"$actual" = "$expected"	]
	then
		echo "Test of ($name) OK (actual value: $actual)"
	else
		echo "Test of ($name) FAILED, EXPECTED VALUE: $expected, ACTUAL VALUE: $actual, WILL ABORT"
		exit 1
	fi

}

function testUrl(){
	
	url=$@
	
	if $url -ks -f -o /dev/null
	then
		return 0
	else
		return 1
	fi;
}

function testCompositeCreated(){
	
	if ! assertCurl 200 "curl $AUTH -k https://$HOST:$PORT/product-composite/$PROD_ID_REVS -s"
	then 
		echo -n "FAIL"
		return 1
	fi
	
	set +e 
	assertEqual "$PROD_ID_REVS" $(echo $RESPONSE | jq .productId)
	if [ "$?" -eq "1" ] ; then return 1; fi
	
	assertEqual 4 $(echo $RESPONSE | jq ".reviews | length")
	if [ "$?" -eq "1" ] ; then return 1; fi
	
	set -e
}

function waitForMessageProcessing(){
	echo "Wait for messages to be proccessed... "
	sleep 1
	
	n=0
	until testCompositeCreated
	do
		n=$((n + 1))
		if [[ $n == 40 ]]
		then
			echo " Give up"
			exit 1
		else
			sleep 6
			echo -n ", retry #$n "
		fi
	done
	echo "All messages are new processed!"
}

function recreateComposite() {
	local productId=$1
	local composite=$2
	
	assertCurl 200 "curl -X DELETE $AUTH -k https://$HOST:$PORT/product-composite/${productId} -s"
	assertEqual 200 $(curl -X POST -k -s https://$HOST:$PORT/product-composite -H "Content-Type: application/json" -H "Authorization: Bearer $ACCESS_TOKEN" --data "$composite" -w "%{http_code}" )
}

function setupTestdata(){

    body="{\"productId\": $PROD_ID_REVS, \"name\": \"name1\", \"weight\": 200, \"reviews\": [ 
       {\"reviewId\": 1, \"author\": \"author 1\", \"subject\": \"subject 1\", \"content\": \"content 1\"}, 
       {\"reviewId\": 2, \"author\": \"author 2\", \"subject\": \"subject 2\", \"content\": \"content 2\"}, 
       {\"reviewId\": 3, \"author\": \"author 3\", \"subject\": \"subject 3\", \"content\": \"content 3\"}, 
       {\"reviewId\": 4, \"author\": \"author 4\", \"subject\": \"subject 4\", \"content\": \"content 4\"} 
     ], 
     \"serviceAdresses\": {} 
    }"
    
    recreateComposite "$PROD_ID_REVS" "$body"

    body="{\"productId\": $PROD_ID_NO_REVS, \"name\": \"name1\", \"weight\": 200, \"reviews\": [], \"serviceAdresses\": {} }"

    recreateComposite "$PROD_ID_NO_REVS" "$body"
}

function testCircuitBreaker(){
	echo "Start Circuit Breaker tests!"
	
	assertEqual "CLOSED" "$(docker compose exec -T product-composite curl -s http://product-composite:8080/actuator/health | jq -r .components.circuitBreakers.details.product.details.state)"

	for((n=0; n<4; n++))
	do
		
		assertCurl 500 "curl -k https://$HOST:$PORT/product-composite/$PROD_ID_REVS?delay=3 $AUTH -s"
		message=$(echo $RESPONSE | jq -r .message)
		assertEqual "Did not observe any item or terminal signal within 2000ms" "${message:0:57}"
	done
	
	assertEqual "OPEN" "$(docker compose exec -T product-composite curl -s http://product-composite:8080/actuator/health | jq -r .components.circuitBreakers.details.product.details.state)"

	assertCurl 200 "curl -k https://$HOST:$PORT/product-composite/$PROD_ID_REVS?delay=3 $AUTH -s"
	assertEqual "fallback product$PROD_ID_REVS" "$(echo "$RESPONSE" | jq -r .name)"
	
	assertCurl 200 "curl -k https://$HOST:$PORT/product-composite/$PROD_ID_REVS $AUTH -s"
	assertEqual "fallback product$PROD_ID_REVS" "$(echo "$RESPONSE" | jq -r .name)"
	
	
	assertCurl 404 "curl -k https://$HOST:$PORT/product-composite/$PROD_ID_NOT_FOUND $AUTH -s"
	assertEqual "product ID= $PROD_ID_NOT_FOUND not found in fallback cache!" "$(echo $RESPONSE | jq -r .message)"
	
	echo "Will sleep for 10 sec waiting for the CB to go Half Open ...."
	sleep 10
	
	assertEqual "HALF_OPEN" "$(docker compose exec -T product-composite curl -s http://product-composite:8080/actuator/health | jq -r .components.circuitBreakers.details.product.details.state)"
	
	for ((n=0; n<3 ;n++))	
	do
	 	assertCurl 200 "curl -k https://$HOST:$PORT/product-composite/$PROD_ID_REVS $AUTH -s"
		assertEqual "name1" "$(echo "$RESPONSE" | jq -r .name)"
	done
	
	assertEqual "CLOSED" "$(docker compose exec -T product-composite curl -s http://product-composite:8080/actuator/health | jq -r .components.circuitBreakers.details.product.details.state)"
	

	assertEqual "CLOSED_TO_OPEN" "$(docker compose exec -T product-composite curl -s http://product-composite:8080/actuator/circuitbreakerevents/product/STATE_TRANSITION | jq -r .'circuitBreakerEvents[-3]'.stateTransition)"
	assertEqual "OPEN_TO_HALF_OPEN" "$(docker compose exec -T product-composite curl -s http://product-composite:8080/actuator/circuitbreakerevents/product/STATE_TRANSITION | jq -r .'circuitBreakerEvents[-2]'.stateTransition)"
	assertEqual "HALF_OPEN_TO_CLOSED" "$(docker compose exec -T product-composite curl -s http://product-composite:8080/actuator/circuitbreakerevents/product/STATE_TRANSITION | jq -r .'circuitBreakerEvents[-1]'.stateTransition)"
	 
	
}

function waitForService(){
	url=$@
	echo -n "wait for: $url..."
	n=0
	until testUrl $url
	do
		n=$((n+1))
		if [[ $n == 100 ]]
		then
			echo " Give up"
			exit 1
		else
			sleep 3
			echo -n ", retry #$n"
		fi
	done
	echo "Done, continues..."
}



set -e

echo "HOST=${HOST}"
echo "PORT=${PORT}"

if [[ $@ == *"start"* ]]
then
	echo "Restarting the test rnvironment"
	echo "$ docker compose down --remove-orphans"
	docker compose down --remove-orphans
	echo "$ docker compose up -d"
	docker compose up -d
fi

waitForService curl -k https://$HOST:$PORT/actuator/health

ACCESS_TOKEN=$(curl -k https://writer:secret-writer@$HOST:$PORT/oauth2/token -d grant_type=client_credentials -d scope="product:read product:write" -s | jq .access_token -r )
echo ACCESS_TOKEN=$ACCESS_TOKEN
AUTH="-H \"Authorization: Bearer $ACCESS_TOKEN\""


assertCurl 200 "curl -H "accept:application/json" -k https://user:password@$HOST:$PORT/eureka/api/apps -s"
assertEqual 5 $(echo $RESPONSE | jq ".applications.application | length")

setupTestdata

waitForMessageProcessing


echo -e "\n"


echo "Start of test: get product with reviews using productId"
assertCurl 200 "curl $AUTH -k https://$HOST:$PORT/product-composite/$PROD_ID_REVS -s" "response status"
assertEqual $PROD_ID_REVS $(echo $RESPONSE | jq .productId) "product id"
assertEqual 4 $(echo $RESPONSE | jq ".reviews | length") "reviews length"
echo "End of test: get product with reviews using productId" 
echo -e "\n"

echo "Start of test: get product with using unkown productId"
assertCurl 404 "curl $AUTH -k https://$HOST:$PORT/product-composite/$PROD_ID_NOT_FOUND -s" "response status"
assertEqual "No product found for productId: $PROD_ID_NOT_FOUND" "$(echo $RESPONSE | jq -r .message)" "response content"
echo "End of test: get product with using unkown productId"
echo -e "\n"

echo "Start of test: get product without reviews using productId"
assertCurl 200 "curl $AUTH -k https://$HOST:$PORT/product-composite/$PROD_ID_NO_REVS -s" "response status"
assertEqual $PROD_ID_NO_REVS $(echo $RESPONSE | jq .productId) "product id"
assertEqual 0 $(echo $RESPONSE | jq ".reviews | length") "reviews length"
echo "End of test: get product without reviews using productId"
echo -e "\n"

echo "Start of test: get product using negative productId"
assertCurl 422 "curl $AUTH -k https://$HOST:$PORT/product-composite/-1 -s" "response status"
assertEqual "\"Invalid productId: -1\"" "$(echo $RESPONSE | jq .message)" "response content"
echo "End of test: get product using negative productId" 
echo -e "\n"

echo "Start of test: get product using string as productId"
assertCurl 400 "curl $AUTH -k https://$HOST:$PORT/product-composite/invalidProductId -s" "response status"
assertEqual "Type mismatch." "$(echo $RESPONSE | jq -r .message)" "response content"
echo "End of test: get product using string as productId" 

echo "Start of test: reader client"
READER_ACCESS_TOKEN=$(curl -k https://reader:secret-reader@$HOST:$PORT/oauth2/token -d grant_type=client_credentials -d scope="product:read" -s | jq .access_token -r)
echo READER_ACCESS_TOKEN=$READER_ACCESS_TOKEN
READER_AUTH="-H \"authorization: Bearer $READER_ACCESS_TOKEN\""
assertCurl 200 "curl $READER_AUTH -k https://$HOST:$PORT/product-composite/$PROD_ID_REVS -s"
assertCurl 403 "curl -X DELETE $READER_AUTH -k https://$HOST:$PORT/product-composite/$PROD_ID_REVS -s"
echo "End of test: readed client"

echo "Swagger/OpenAPI tests"
assertCurl 302 "curl -ks https://$HOST:$PORT/openapi/swagger-ui.html"
assertCurl 200 "curl -ksL https://$HOST:$PORT/openapi/swagger-ui.html"
assertCurl 200 "curl -ks https://$HOST:$PORT/openapi/webjars/swagger-ui/index.html?configUrl=/V3/api-docs/swagger-config"
assertCurl 200 "curl -ks https://$HOST:$PORT/openapi/v3/api-docs"
assertEqual "3.0.1" "$(echo $RESPONSE | jq -r .openapi)"
assertEqual "https://$HOST:$PORT" "$(echo $RESPONSE | jq -r '.servers[0].url')"
assertCurl 200 "curl -sk https://$HOST:$PORT/openapi/v3/api-docs.yaml"


echo -e "\n"

if [[ $SKIP_CB_TESTS == "false" ]]
then
	testCircuitBreaker
fi


if [[ $@ == *"stop"* ]]
then
	echo "We are done, stopping the test environment..."
	echo "$ docker compose down"
	docker compose down 
fi

echo "END, all test OK:" `date`





















