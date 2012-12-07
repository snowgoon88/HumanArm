# Makefile pour compiler
# Dans ce répertoire faire
# >make class

PROJET = HumanArm
VERSION = 2.0

HOMEDIR = HumanArm

SRC =     model/Arm.java \
          model/ArmModelListener.java \
          model/CompleteArm.java \
          model/NeuroControl.java \
          model/SimpleMuscle.java \
          model/Command.java \
          model/CommandSequence.java \
          model/CommandSequenceListener.java \
          model/Model.java \
          model/ModelListener.java \
          utils/JamaU.java \
          utils/GraphicHelpe.java \
          viewer/JArm2D.java \
          viewer/JArmLabel.java \
          viewer/JCommand.java \
          viewer/JCommandSequence.java \
          example/ArmConsole.java \
          example/ArmGraphic.java \
          example/ExperienceConsole.java \
          gui/ExpGUI.java \
          gui/JExperience.java

# Regle pour compiler les classes
.PHONY:	class
class:
	javac -cp ../libs/jama-1.0.2.jar:../libs/jchart2d-3.2.2.jar:../libs/vecmath-1.5.2.jar:. $(SRC)





