#!/usr/bin/env bash
#
#
#
: ${HOST=localhost}
: ${PORT=8080}
: ${PROD_ID_REVS=1}
: ${PROD_ID_NOT_FOUND=12}
: ${PROD_ID_NO_REVS=213}

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
	
	if ! assertCurl 200 "curl http://$HOST:$PORT/product-composite/$PROD_ID_REVS -s"
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
	
	assertCurl 202 "curl -X DELETE http://$HOST:$PORT/product-composite/${productId} -s"
	asserCurl 202 $(curl -X POST -s http://$HOST:$PORT/product-composite -H "Content-Type: application/json" --data "$composite" -w "%{http_code}" )
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

function recreateComposite(){
	local productId=$1
	local composite=$2
	
	assertCurl 200 "curl -X DELETE http://$HOST:$PORT/product-composite/${productId} -s"
	curl -X POST http://$HOST:$PORT/product-composite -H "Content-Type: application/json" --data "$composite"
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

waitForService curl http://$HOST:$PORT/actuator/health

setupTestdata

waitForMessageProcessing


echo -e "\n"


echo "Start of test: get product with reviews using productId"
assertCurl 200 "curl http://$HOST:$PORT/product-composite/$PROD_ID_REVS -s" "response status"
assertEqual $PROD_ID_REVS $(echo $RESPONSE | jq .productId) "product id"
assertEqual 4 $(echo $RESPONSE | jq ".reviews | length") "reviews length"
echo "End of test: get product with reviews using productId" 
echo -e "\n"

echo "Start of test: get product with using unkown productId"
assertCurl 404 "curl http://$HOST:$PORT/product-composite/$PROD_ID_NOT_FOUND -s" "response status"
assertEqual "No product found for productId: $PROD_ID_NOT_FOUND" "$(echo $RESPONSE | jq -r .message)" "response content"
echo "End of test: get product with using unkown productId"
echo -e "\n"

echo "Start of test: get product without reviews using productId"
assertCurl 200 "curl http://$HOST:$PORT/product-composite/$PROD_ID_NO_REVS -s" "response status"
assertEqual $PROD_ID_NO_REVS $(echo $RESPONSE | jq .productId) "product id"
assertEqual 0 $(echo $RESPONSE | jq ".reviews | length") "reviews length"
echo "End of test: get product without reviews using productId"
echo -e "\n"

echo "Start of test: get product using negative productId"
assertCurl 422 "curl http://$HOST:$PORT/product-composite/-1 -s" "response status"
assertEqual "\"Invalid productId: -1\"" "$(echo $RESPONSE | jq .message)" "response content"
echo "End of test: get product using negative productId" 
echo -e "\n"

echo "Start of test: get product using string as productId"
assertCurl 400 "curl http://$HOST:$PORT/product-composite/invalidProductId -s" "response status"
assertEqual "Type mismatch." "$(echo $RESPONSE | jq -r .message)" "response content"
echo "End of test: get product using string as productId" 

echo "Swagger/OpenAPI tests"
assertCurl 302 "curl -s http://$HOST:$PORT/openapi/swagger-ui.html"
assertCurl 200 "curl -sL http://$HOST:$PORT/openapi/swagger-ui.html"
assertCurl 200 "curl -s http://$HOST:$PORT/openapi/webjars/swagger-ui/index.html?configUrl=/V3/api-docs/swagger-config"
assertCurl 200 "curl -s http://$HOST:$PORT/openapi/v3/api-docs"
assertEqual "3.0.1" "$(echo $RESPONSE | jq -r .openapi)"
assertEqual "http://$HOST:$PORT" "$(echo $RESPONSE | jq -r '.servers[0].url')"
assertCurl 200 "curl -s http://$HOST:$PORT/openapi/v3/api-docs.yaml"


echo -e "\n"

if [[ $@ == *"stop"* ]]
then
	echo "We are done, stopping the test environment..."
	echo "$ docker compose down"
	docker compose down 
fi

echo "END, all test OK:" `date`





















