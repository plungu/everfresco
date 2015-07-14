### Building/developing Everfresco ###

1. Download the source

  * svn checkout http://everfresco.googlecode.com/svn/trunk/ EverFresco

2. Go to the EverFresco directory configure the build.properties with the location of the alfresco-enterprise-sdk-4.1.3.x

Example: alfresco.sdk.dir=/mnt/hgfs/Downloads/alfresco-downloads/releases/alfresco-enterprise-sdk-4.1.3.22

3. Run the ant build. Simply run "ant" at the command line for the default build. Be sure you are in the EverFresco base directory. You will see the output of the ant build in EverFresco/build directory.