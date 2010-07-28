
Standalone agent that just logs install/uninstall instructions to stdout.

To run:

 mvn -Prun.logging.agent
 

Properties may be set by creating an "agent.properties" file.  The properties 
that can be set are:

  agent.repository.url
  agent.profile
  agent.user
  agent.type
  agent.link
  agent.packages
  