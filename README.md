# Karrenkauf
das coolste asw Projekt.

## Frontend

### Init
1. Once cloned, Change the Directory to ```Karrenkauf/Frontend```
2. run ```npm install``` to install all dependencies. NOTE: NODEJS MUST BE INSTALLED!
3. ```npm run dev``` in order to actually run it.
4. follow the localhost url the the website

### Example Lists
If wanted, import the example.json file over the burger menu to test all functions. 


## Backend API Endpoints

### Quick Test 😊
http://localhost:8080/api/hello

### User Create
Create users. Newly created users will be stored on the user table in backend.db.
You can send such requests in the browser console like so:

```
// Replace 'newuser' and 'newpassword' with desired credentials
fetch("http://localhost:8080/api/user?username=newuser&password=newpassword", {
    method: "POST"
})
.then(res => res.text())   // read response as plain text
.then(console.log)         // print success or error message
.catch(err => console.error("Error:", err));
```

### User Login
Needless to say, login for users. If login successful, token will be stored in backend.db in backend folder.
You can send such request in the browser console.

```
// Replace 'user' and 'password' with your credentials
fetch("http://localhost:8080/api/login?username=user&password=password", {
    method: "POST"
})
.then(res => res.text())   // read response as plain text
.then(console.log)         // print token or error message
.catch(err => console.error("Error:", err));
```

### Status User Logon
Can be called in browser URL bar like so:

http://localhost:8080/api/status?token=XXX

Either returns status unknown token, valid token or expired token.
Token entries are stored in the tokens table in backend.db.
Expiration date set to 1 hour, should be sufficient.

## TODO
1. Actual password hashing 💀
2. Populating list table (table already exists in backend.db)
3. Proper login dialog in frontend?
4. All that sync list stuff
