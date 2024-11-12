<div align="center">

# **_S.C.O.O.F.S_**

**Scoped Open Object File Storage**: secured and handly encrypted storage manager.

</div>

## Mission

The mission is to provide data managed with safe way to store data with possibility to access anoly with prevously created key.

## Keys

By this app user can create keys, when use that keys to create containers to store data. Each key can be used for create unlimited amount of encrypted data containers.
Keys generation provide keystore file which can be located in device folders or is USB MSD with FAT32 file system.
Each app session requires to load key before work. Loaded keystore exist only in ORM, application does not creates local copy for security proporse. So, after process finishes user shall load the key again.
Keystore can be created with uniq users password and loaded only with it.

## USB MSD

Currently S.C.O.O.F.S works only with MSD with FAT32. Suggested way to use MSD is to store keystores in it. All keys can be generated directly on MSD and can be loaded from.
Also there is posibility to encrypt and add to container files

## CONTAINERS

All data what user would like to encrypt and store locates in container file. Container file can be created by user before he encrypts and adds his first file. If there are several containers, user shall select witch one he wants to store his data.
All containers can be removed by user or if emergency password was entered. Before container creation user shall create (if nessesarry) and load keys from keystore file. Each container contains user files encrypted with independed keys with tag system.
All containers can be opened with suitable loaded keys (keys what were used to create that container) like a folder.

## DECRYPTION

When container is opened user can select files (or all files) he would like to decrypt. All decrypted files will be stored on user's device.


