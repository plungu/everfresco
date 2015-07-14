### Installing Everfresco ###

1. Go to [Release](Release.md) page below to download the Repo and Share amps:

2. Make sure Alfresco is stopped

3. Copy the repo amp to alf\_install\_dir/amps

4. Copy the share amp to alf\_install\_dir/share\_amps

5. Run at the command prompt: alf\_install\_dir/bin/apply\_amps.sh (or use the mmt jar)

6. Start Alfresco

7. Check that the module is installed by opening alfresco.log. This message will let you know it was successfully installed:

20:18:32,289 INFO [org.alfresco.repo.module.ModuleServiceImpl?] Starting module 'EverFresco?' version 1.0. 20:18:32,296 DEBUG [org.alfresco.repo.module.ModuleComponentHelper?] Started module 'ModuleDetails?[{module.version=1.0, module.description=Evernote Alfresco integration project, module.id=EverFresco?, module.repo.version.max=999, module.title=EverFresco?, module.repo.version.min=0, module.installState=INSTALLED, module.installDate=2014-05-11T20:17:23.675-07:00}]' including 0components.


### Setting up a channel ###

1. Log in to Share as admin go to Channel Manager in Admin Panel

2. Select Everfresco from the Dropdown. The initial authorization will fail. You need to configure everfresco first. Read on.

3. Click on the channel icon.

4. In the form enter the following:

  * consumerKey

  * consumerSecret

  * Tick to use as Default channel (this is not yet available in this build, Everfresco uses the first channel you configure as default)

  * Tick Use Sandbox to point Everfresco to the developer sandbox else it will point to production Evernote account.

Note: this requires setting up a Evernote account and service keys

5. Save the form.

6. Click to reauthorize Everfresco.

7. Go to a doc and sync with your Evernote account.


### Applying permissions and using multiple channels ###

1. To allow only specific users to use the channel you can apply groups and/or users to each channel you configure.

2. Go to Channel Manager click the permissions link

3. remove the Inherit Permissions by clicking the link

4. click add users and groups. Add a group with the users you want to be able to publish to the configured Evernote account.

5. Any number of Evernote accounts can be configured. Permissions can be used to segregate them to specific users and groups.


### Syncing docs ###

Use the Publish action for syncing when you need to choose between multiple channels (This can be configured by setting up multiple channels and define permissions for users and groups)

Use the Everfresco DocLib action when you want to quick sync with your default channel. (If you have permissions set on the default channel the action WILL respect the permissions. i.e. It will only let users publish who have permission to the channel.


### See [DevelopEverfresco](DevelopEverfresco.md) for more on building and developing from source ###