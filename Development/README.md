# team-201-SP19
## Team Members & Authors

- Ketan Kale ([kaleketan07](https://github.ccs.neu.edu/kaleketan07))
- Kunal Patil ([kspatil](https://github.ccs.neu.edu/kspatil))
- Rahul Bhat ([rahulbhat31](https://github.ccs.neu.edu/rahulbhat31))
- Sachin Haldavanekar ([sachinh](https://github.ccs.neu.edu/sachinh))

# CS5500-Prattle Server
Prattle is a network server that communicates with IM clients that connect to it.  The capabilities of the server have been described below:

## Current Version
2.0

## Prerequisites/ Installations:
- `Java 8`

## Status

![Build Status](https://www.5500jenkins-2.cciscloud.com/job/team-201-SP19/job/master/badge/icon)


## Features

The following handles can be used to use the client jar to interact with other clients when the Prattle server is running:

|Handle|Description|Syntax|Status|
|-------|------------|-------|-------|
|LGN|Login|`LGN;{password}`|Completed|
|REG|Register|`REG;{password};{confirm password}`|Completed|
|BYE|Quit|`BYE`|Completed|
|BCT|Broadcast a message to every user present on Prattle|`BCT;{Message}`|Completed|
|MSU|Message user (To send a private message to another user)|`MSU;{Message};{username}`|Completed|
|UPU|Update User Profile: Each attribute is mapped to a fixed number. The attributes with their input types are: `1:First name {Any String} 2:Last name {Any String} 3:Password {Any String} 4:Searchability {1/True 0/False}`|`UPU;{attributeNumber};{Value}`|Completed|
|CRG|Create a group|`CRG;{New Group Name}`|Completed|
|DEG|Delete a group|`DEG;{groupName}`|Completed|
|GTG|Get the details for a group|`GTG;{groupName}`|Completed|
|UPG|Update the settings of a group. Each attribute is mapped to a fixed number. The attributes with their input types are: 1:Group Searchability {1/True 0/False}|`UPG;{GroupName};{attributeNumber}:{Value}`|Completed|
|AUG|Add a user to an existing group|`AUG;{Userame};{GroupName}`|Completed|
|RUG|Delete a user from the group|`RUG;{Username};{GroupName}`|Completed|
|MSG|Send a message in a group|`MSG;{Message text};{GroupName}`|Completed|
|DGM|Delete a group message|`DGM;{Message Key}`|Completed|
|PRE|Privately reply to a group message|`PRE;{Message Text};{Message Key}`|Completed|
|DPM|Delete a private message|`DPM;{Message Key}`|Completed|
|GFR|Get all your followers|`GFR`|Completed|
|GFE|Get all the users you are following|`GFE`|Completed|
|FWU|Follow a user|`FWU;{Username}`|Completed|
|UFU|Unfollow a user|`UFU;{Username}`|Completed|
|SRH|Search for a user|`SRH;User;{Search String}`|Completed|
|SRH|Search for a group|`SRH;Group;{Search String}`|Completed|                                    
|DLU|Delete a user account|`DLU`|Completed|
|IUG|Invite a user to join a group that you are part of|`IUG;{Username};{Groupname}`|Completed|
|DUI|Delete an invitation sent by you to a user|`DUI;{Username};{Groupname}`|Completed|
|AIU|Accept an invitation sent to you by another user|`AIU;{Groupname}`|Completed|
|DIU|Deny an invitation sent to you by another user|`DIU;{Groupname}`|Completed|
|AIM|Approve an invitation for a group, of which you are a moderator|`AIM;{Username};{Groupname}`|Completed|
|RIM|Reject an invitation for a group, of which you are a moderator|`RIM;{Username};{Groupname}`|Completed|
|GPM|Get all your past messages|`GPM`|Completed|
|GOU|Get online users that you are following|`GOU`|Completed|
|LGP|Leave group|`LGP;{GroupName}`|Completed|

## Tests

This is a maven project, and all tests are written using JUnit 5.

You can run the tests using the command `mvn test`

## How to use?

1. Clone this repository. The steps to clone a git repository can be found [here](https://help.github.com/en/articles/cloning-a-repository)
2. Get the prattle.main() running on some port P
3. Connect using the client jar (provide the hostname and port P). The latest Client jar can be found [at this location](https://github.ccs.neu.edu/cs5500/team-201-SP19/blob/master/Development/ChatServer/src/main/resources/Chatter-0.0.1-SNAPSHOT-jar-with-dependencies.jar)
4. Communicate with different clients using the message handles provided in the table above.

## Credits

Credits to the instructors and TAs of CS5500 for guiding us throughout the project and providing the started code. 
Also credit to other teams who helped out resolving common problems and classmates who provided valuable feedback during the in class codewalk.

## License

This work is licensed under the Creative Commons Attribution-ShareAlike 4.0 International License. To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/4.0/. It is based on work originally written by Matthew Hertz and has been adapted for use in a class assignment at Northeastern University.
