# spring-auth-demo

Project to build a spring based micro service for authentication and authorisation 
using json web tokens for communication. Its main purpose is to demonstrate how 
additional authentication and login filters can be plugged into the
spring security architecture.

## authentication
is the core project storing user and role data in a relational database and exposing a
REST controller interface for all administration tasks. 

## authclient
demonstrates using the authentication service in a number of test cases. Plan is to add
a demo web app client later on.

## authutil
has utility classes used by other modules and base is for integration into other projects.
The rest api with use of secured json web tokens is fully encapsulated in implementations of
the AdminAccess interface.

## acl3
is the empty wrapper project for the other modules.

