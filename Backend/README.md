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

## List Endpoints

### Lists Sync
Sync lists from the client. Existing lists in the database will be overwritten if IDs match.  

**Endpoint:**  
POST /api/lists/sync

**Body:** JSON object in the format:
```
{
    "lists": {
        "listId1": {
            "id": "listId1",
            "name": "Shopping List",
            "owner": "user123",
            "createdAt": 1700000000000,
            "updatedAt": 1700000000000,
            "items": [
                {
                    "id": "item1",
                    "name": "Milk",
                    "beschreibung": "2 liters",
                    "menge": 2,
                    "unit": "liter",
                    "preis": 300,
                    "done": false,
                    "checked": false,
                    "category": "Dairy",
                    "createdAt": 1700000000000,
                    "updatedAt": 1700000000000
                }
            ]
        }
    }
}
```

### Get All Lists
Retrieve all lists stored in the database.

**Endpoint:**  
GET /api/lists

**Body:** 
```
fetch("http://localhost:8080/api/lists")
  .then(res => res.json())
  .then(console.log)
  .catch(err => console.error("Error:", err));
```

### Get List By ID
Retrieve a specific list by its ID.

**Endpoint:**  
GET /api/lists/{id}

**Body:** 
```
fetch("http://localhost:8080/api/lists/listId1")
  .then(res => res.json())
  .then(console.log)
  .catch(err => console.error("Error:", err));
```

## List Sharing Endpoints

### Share List with User
Share a list with another user by username. Only the list owner can share.

**Endpoint:**  
POST /api/lists/{listId}/share

**Headers:**  
Authorization: Bearer {token}

**Body:**
```json
{
  "username": "targetuser"
}
```

**Example:**
```javascript
fetch("http://localhost:8080/api/lists/listId1/share", {
    method: "POST",
    headers: {
        "Content-Type": "application/json",
        "Authorization": "Bearer YOUR_TOKEN_HERE"
    },
    body: JSON.stringify({ username: "targetuser" })
})
.then(res => res.text())
.then(console.log)
.catch(err => console.error("Error:", err));
```

### Get Shared Users for a List
Get the list of users a list is shared with.

**Endpoint:**  
GET /api/lists/{listId}/shared

**Headers:**  
Authorization: Bearer {token}

**Response:**
```json
{
  "listId": "listId1",
  "owner": "ownerUsername",
  "sharedWith": ["user1", "user2"]
}
```

**Example:**
```javascript
fetch("http://localhost:8080/api/lists/listId1/shared", {
    headers: {
        "Authorization": "Bearer YOUR_TOKEN_HERE"
    }
})
.then(res => res.json())
.then(console.log)
.catch(err => console.error("Error:", err));
```

### Remove User from Shared List
Remove a user's access to a shared list. Only the list owner can do this.

**Endpoint:**  
DELETE /api/lists/{listId}/share/{username}

**Headers:**  
Authorization: Bearer {token}

**Example:**
```javascript
fetch("http://localhost:8080/api/lists/listId1/share/targetuser", {
    method: "DELETE",
    headers: {
        "Authorization": "Bearer YOUR_TOKEN_HERE"
    }
})
.then(res => res.text())
.then(console.log)
.catch(err => console.error("Error:", err));
```

### Get Accessible Lists
Get all lists the user can access (both owned and shared with them).

**Endpoint:**  
GET /api/lists/accessible

**Headers:**  
Authorization: Bearer {token}

**Response:**
```json
{
  "owned": [ /* array of lists owned by user */ ],
  "shared": [ /* array of lists shared with user */ ]
}
```

**Example:**
```javascript
fetch("http://localhost:8080/api/lists/accessible", {
    headers: {
        "Authorization": "Bearer YOUR_TOKEN_HERE"
    }
})
.then(res => res.json())
.then(console.log)
.catch(err => console.error("Error:", err));
```