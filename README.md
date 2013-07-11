Simple implementation of the Trivial File Transfer Protocol
-----------------------------------------------------------

Known issues:
 - differs from the official RFC in that it treats attempts to write to existing remote files as errors, instead of **only** allowing writing to already-existing remote files
 - currently uses the built-in Java serialization for the creation of datagrams (Yuck!), so there's quite a lot of overhead (100+ Bytes instead of 2-4 per datagram, ouch)

Author: Andrei Barsan
License: BSD 2-Clause License
