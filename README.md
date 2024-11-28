Using openjdk-22

#### Design Considerations
My intuition is to create a node manager server instance that has members connect to it and send messages to propagate or specifically respond to a peer through
This opens the system up to a single point of failure. Since the node-manager would be trying to be transparent, if it closed the members would not know if it was an issue with the other members or the server


|       Member | Vote |       Response        |
|-------------:|:----:|:---------------------:|
|           M1 |  M1  |       Immediate       |
|           M2 |  M2  |  Slow \|\| Immediate  |
|           M3 |  M3  | Moderate \|\| Offline |
|         M4-9 | Any  | Vary (avg. Moderate)  |

Nodes agree on a President
Day of the vote:
1. One node sends a proposed president to all nodes
2. A majority is required (half + 1)

### Message Design
#### Message Codes
| Status Code |     Status     |
|------------:|:--------------:|
|         100 |    PROPOSE     |
|         101 |   PREPARE-OK   |
|         102 |      NACK      |
|         200 | ACCEPT-REQUEST |
|         201 |   ACCEPT-OK    |
|         202 | ACCEPT-REJECT  |
|         300 |     DECIDE     |

#### Message Templates
Body is the proposal id and proposed nominee
```
DS3/1.0 100 PROPOSE
{ "n": val, "nominee": val }
```
Body is the last accepted proposal id and proposed nominee
```
DS3/1.0 101 PREPARE-OK or 101 NACK
{ "n": val, "nominee": val }
```

```
DS3/1.0 200 ACCEPT-REQUEST

```
```
DS3/1.0 201 ACCEPT-OK or 202 ACCEPT-REJECT
```
```
DS3/1.0 300 DECIDE
{ "n": val, "nominee": val }
```
There are several assumptions made on the part of the recipient.
1. That there are only alphanumerical characters used in keys and values
2. Messages use utf-8 encoding
3. That headers can be delimited by `' '` and `'\n'`

We are assuming that:
1. We know the exact number of members in the network, and thus the number of members required for a majority
2. 