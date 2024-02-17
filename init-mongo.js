db.getSiblingDB("admin")
.createUser({
  user: "documents",
  pwd: "password",
  roles: [{ role: "readWrite", db: "documents" }],
});