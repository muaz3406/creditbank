# creditbank

Highlights

Dockerize application
\n swagger and openapi documentation (http://localhost:8081/swagger-ui/index.html)
\n JWT authentication
\n liquibase (ddl for tables and dml for installment rules and exceptio messages)
\n caching by redis ( caching auth and exception messages)
\n exception messages with language type
\n exception messages with traceId

# how to run
docker-compose up -d

# how to test
postmane collection has prepared requests

# create-user
this two request for testing not for real world
curl --location 'http://localhost:8081/api/v1/auth/create' \
--header 'Content-Type: application/json' \
--header 'Cookie: JSESSIONID=C53DA118175A95C3A9D0585BC6BEF4A5' \
--data '{
    "username": "muaz",
    "password": "12345",
    "roles": ["ROLE_CUSTOMER"]
}'

# create-admin
curl --location 'http://localhost:8081/api/v1/auth/create' \
--header 'Content-Type: application/json' \
--header 'Cookie: JSESSIONID=763DB13D4A5B7AFDCE51A6E33D211F8D' \
--data '{
    "username": "admin",
    "password": "admin",
    "roles": ["ROLE_ADMIN"]
}'

# create customer
admin creates a customer, payload has username to match user and customer
# create loan
admin creates a loan
# search loan by filter payload
# get loan by id
admin and customer(limited) can use
# get installments by loan id
admin and customer(limited) can use
# pay loan
admin and customer(limited) can use
