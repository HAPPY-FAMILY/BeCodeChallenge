db = db.getSiblingDB('logs_db');

db.createUser({
    user: "root",
    pwd: "123456",
    roles: [
        { role: "dbOwner", db: "logs_db" },
        "readWrite"
    ]
});
