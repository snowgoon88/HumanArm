# Makefile 
# Pour compiler
# >make class
# Pour lancer l'expérience exemple
# >make re
# Pour lancer le programme en mode console ou graphique
# >make rc
# >make rg
# Pour creer l'archive
# >make tgz

PROJET = HumanArm
VERSION = 1.5

HOMEDIR = HumanArm

TAR_SRC = $(HOMEDIR)/src/example \
          $(HOMEDIR)/src/model \
          $(HOMEDIR)/src/utils \
          $(HOMEDIR)/src/viewer/JArm2D.java \
          $(HOMEDIR)/src/viewer/JArmLabel.java

TAR_LIBS = $(HOMEDIR)/libs

TAR_DIV = $(HOMEDIR)/readme.txt \
          $(HOMEDIR)/Makefile \
          $(HOMEDIR)/src/Makefile \
          $(HOMEDIR)/data/consigne_example.data

# Regle pour compiler
.PHONY: class
class:
	make -C src

# Regle pour creer l'archive
.PHONY:	tgz
tgz:	
	tar czvf $(PROJET)_src_$(VERSION).tgz -h -C .. --exclude=".git" --exclude="*~" $(TAR_SRC) $(TAR_LIBS) $(TAR_DIV)

# Regle pour lancer l'experience
.PHONY: re
re:
	java -cp libs/jama-1.0.2.jar:libs/jchart2d-3.2.2.jar:libs/vecmath-1.5.2.jar:src example/ExperienceConsole

# Regle pour lancer en mode console
.PHONY: rc
rc:	
	java -cp libs/jama-1.0.2.jar:libs/jchart2d-3.2.2.jar:libs/vecmath-1.5.2.jar:src example/ArmConsole

# Regle pour lancer en mode graphic
.PHONY: rg
rg:	
	java -cp libs/jama-1.0.2.jar:libs/jchart2d-3.2.2.jar:libs/vecmath-1.5.2.jar:src example/ArmGraphic

	