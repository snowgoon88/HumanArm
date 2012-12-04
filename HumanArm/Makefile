# Makefile pour creer l'archive
# Dans ce répertoire faire: make tgz

PROJET = HumanArm
VERSION = 1.0

HOMEDIR = HumanArm

TAR_SRC = $(HOMEDIR)/src/example \
          $(HOMEDIR)/src/model \
          $(HOMEDIR)/src/utils \
          $(HOMEDIR)/src/viewer/JArm2D.java 

TAR_LIBS = $(HOMEDIR)/libs

TAR_DIV = $(HOMEDIR)/readme.txt \
          $(HOMEDIR)/Makefile \
          $(HOMEDIR)/src/Makefile 

# Regle pour creer l'archive
.PHONY:	tgz
tgz:	
	tar czvf $(PROJET)_src_$(VERSION).tgz -h -C .. --exclude=".git" --exclude="*~" $(TAR_SRC) $(TAR_LIBS) $(TAR_DIV)
