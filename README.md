# Karenkauf
ein asw Projekt.

# Backend API Endpoints

## Quick Test 😊
http://localhost:8080/api/hello

## User Login
http://localhost:8080/api/login?username=XXX&password=XXX

Needless to say, login for users. 
If login successful, token will be stored in auth.db in backend folder.

## Status User Logon
http://localhost:8080/api/status?token=XXX

Either returns status unknown token, valid token or expired token.
Token entries are stored in the auth.db in backend folder.
Expiration date set to 1 hour, should be sufficient.

