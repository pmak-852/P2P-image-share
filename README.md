# P2P-image-share (COMP2396B OOP & Java Assignment 4)

## Intro
This is an image shareing system using P2P protocol. An image will be sliced and shared among all the participants in the network.

## Workflow
There are 2 roles, <b>1. Teacher (server)</b> and <b>2. Student (client)</b>

## Role
### Teacher (Server)
1. choosing the image to share between peers. Peers will update their image through the P2P process when the teacher changes the image.
2. maintain a full list of active users by performing an active check in every 5 sec

### Student (Peer)
1. Actively ask for image block which they don't have. The number of requests is limited to 5 at any time.
2. Respond to other peers request. Student will send the image block, if available, to the requestor.

