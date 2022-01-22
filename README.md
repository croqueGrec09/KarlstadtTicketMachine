# KarlstadtTicketMachine
This is a test for -whenever I have time- setting up a Jenkins for GentzApp/KtM-deploy

This project is part of the Power of Data repository, retrievable under https://github.com/croqueGrec09/KeletiPu

To make this project running:
- change the absolute path in TicketMachine.java (this should be a configuration variable anyway)
- make sure that index is not existing when updating Lucene version
- if running with Java >= 16, Maven may not cope with the version. Insert then in the VM options the following line: --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.lang.reflect=ALL-UNNAMED --add-opens java.base/java.text=ALL-UNNAMED --add-opens java.desktop/java.awt.font=ALL-UNNAMED
