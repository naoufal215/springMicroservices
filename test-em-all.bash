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

waitForService curl http://$HOST:$PORT/product-composite/$PROD_ID_REVS
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
assertEqual null "$(echo $RESPONSE | jq -r .message)" "response content"
echo "End of test: get product using string as productId" 
echo -e "\n"

if [[ $@ == *"stop"* ]]
then
	echo "We are done, stopping the test environment..."
	echo "$ docker compose down"
	docker compose down 
fi

echo "END, all test OK:" `date`





















