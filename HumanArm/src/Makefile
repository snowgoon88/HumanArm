# Makefile pour compiler
# Dans ce répertoire faire
# >make class
# Pour lancer le programme en mode console ou graphique
# >make rc
# >make rg

PROJET = HumanArm
VERSION = 1.0

HOMEDIR = HumanArm

SRC =     model/Arm.java \
          model/CompleteArm.java \
          model/NeuroControl.java \
          model/SimpleMuscle.java \
          utils/JamaU.java \
          viewer/JArm2D.java \
          example/ArmConsole.java \
          example/ArmGraphic.java 

# Regle pour compiler les classes
.PHONY:	class
class:
	javac -cp ../libs/jama-1.0.2.jar:../libs/jchart2d-3.2.2.jar:../libs/vecmath-1.5.2.jar:. $(SRC)

# Regle pour lancer en mode console
.PHONY: rc
rc:	
	java -cp ../libs/jama-1.0.2.jar:../libs/jchart2d-3.2.2.jar:../libs/vecmath-1.5.2.jar:. example/ArmConsole
# Regle pour lancer en mode graphic
.PHONY: rg
rg:	
	java -cp ../libs/jama-1.0.2.jar:../libs/jchart2d-3.2.2.jar:../libs/vecmath-1.5.2.jar:. example/ArmGraphic




