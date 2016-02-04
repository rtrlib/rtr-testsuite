# rtr-testsuite
## About

rtr-testsuite is a basic [rpki-rtr](https://tools.ietf.org/html/rfc6810) implementation that offers
some additional functionality to allow the user to manually manipulate ROA entries.

## Dependencies
rtr-testsuite requires Java 1.7 or higher.

## Install
Build with
```
maven clean
maven package assembly:single
```
which should result in a .tar.gz file that includes everything needed to run except a java runtime.


## Usage
```./rtr-testsuite.sh <port>``` 
whereas port is an optional argument that determines the port the
rtr-testsuite is listening for clients.
Use ```help``` on the rtr-testsuite prompt to get information about available commands.




