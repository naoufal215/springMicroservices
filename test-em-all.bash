#!/usr/bin/env bash
#
#
#
: ${HOST=localhost}
: ${PORT=7000}
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
	
	if [	"$actual" = "$expected"	]
	then
		echo "Test OK (actual value: $actual)"
	else
		echo "Test FAILED, EXPECTED VALUE: $expected, ACTUAL VALUE: $actual, WILL ABORT"
		exit 1
	fi

}

set -e

echo "HOST=${HOST}"
echo "PORT=${PORT}"


assertCurl 200 "curl http://$HOST:$PORT/product-composite/$PROD_ID_REVS -s"
assertEqual $PROD_ID_REVS $(echo $RESPONSE | jq .productId)
assertEqual 4 $(echo $RESPONSE | jq ".reviews | length")

assertCurl 404 "curl http://$HOST:$PORT/product-composite/$PROD_ID_NOT_FOUND -s"
assertEqual "No product found for productId: $PROD_ID_NOT_FOUND" "$(echo $RESPONSE | jq -r .message)"

assertCurl 200 "curl http://$HOST:$PORT/product-composite/$PROD_ID_NO_REVS -s"
assertEqual $PROD_ID_NO_REVS $(echo $RESPONSE | jq .productId)
assertEqual 0 $(echo $RESPONSE | jq ".reviews | length")

assertCurl 422 "curl http://$HOST:$PORT/product-composite/-1 -s"
assertEqual "\"Invalid productId: -1\"" "$(echo $RESPONSE | jq .message)"

assertCurl 400 "curl http://$HOST:$PORT/product-composite/invalidProductId -s"
assertEqual null "$(echo $RESPONSE | jq -r .message)"

echo "END, all test OK:" `date`





















