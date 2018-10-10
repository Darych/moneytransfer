# Money Transfer

REST API to simple money transfer.

## Create account
**Request:**
`POST localhost:8080/account`
```json
{
"name": "user1",
"balance": 100
}
```
**Response on success:** `201 Created`
```json
{
"name": "user1",
"balance": 100.0,
"id": 1
}
```
**Response if input JSON is invalid:** `400 Bad request`

## Get account info
**Request:** `GET localhost:8080/account/{id}`

**Response on success:** `200 OK`
```json
{
    "name": "user1",
    "balance": 100,
    "id": 1
}
```
**Response if id could not be parsed:** `400 Bad Request`

**Response if there was server error:** `500 Internal Server Error`

## Make transaction
**Request:**
`POST localhost:8080/transaction`
```json
{
"from": 1,
"to": 2,
"amount": 25
}
```
**Response on success:** `200 OK`

**Response if transaction JSON could not be parsed:** `400 Bad Request`

**Response if transaction processing failed (no account, no money, transfer to the same account):** `406 Not Acceptable`


This project is MIT licensed.
