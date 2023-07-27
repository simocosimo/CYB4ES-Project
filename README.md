# Techniques for authentication of mobile devices

In the modern world, digital communication between people is key. With different Instant messaging applications being installed on every device, keeping in touch with people has never been so easy. Not only text, but the possibility of exchanging pictures, videos, audio, and files have drastically increased the quality of the conversations that are possible with friends, family, and also between the work environment.

But, as often happens with digital infostructures, the needs from the cybersecurity point of view increase too, specifically when it comes to integrity and authentication of the sent and received messages.
Cryptographic protocols, such as TLS, are specifically designed to solve these problems but this project aims to gain even more details about a device that sent a specific message.

In the use case that's being considered, a device sends a message to the server, and after some time (possibly months or even years) a party wants to check if that message was really sent by that device, and not by some malicious third party. In this protocol, devices need to send some proof about messages that they send, but this proof has to be computed with some information that is strictly bound to the specific device, so that is highly improbable that a malicious actor succeeds in impersonating another device, not in his possession.

To do this, unique identifiers are used: these codes are unique per device and it is difficult to replicate them in different hardware and software, making them the perfect tool to generate the wanted proof.
Some examples of the utility of this protocol could be the following:
 * Law enforcement leading an investigation on a particular message that contains, for example, illegal material or planning dangerous acts that may impact the safety of one person or a group of people
* An IoT system that comprehends a lot of devices that communicate with a central server (a security system, for example) and that needs to be sure that data coming from sensors is coming from the real sensors, and not from fake ones that an attacker could have inserted into the systems

This is the official repository for the project.
