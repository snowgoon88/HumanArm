HumanArm
--------

Quelques classes java pour simuler un bras humain très simple.

16/04/2013 - v2.2 - Interface graphique plus évoluée.
07/12/2012 - v2.0 - Application GUI évoluée pour l'exemple
05/12/2012 - v1.5 - ArmGraphic amélioré et Experience avec sauvegarde
04/12/2012 - v1.0 - Fonctionnel avec exemples console et graphic

Les bases
=========
Les commentaires dans les classes sont assez explicites.

La classe principale du modèle est 'model/CompleteArm'. Il y a en gros 3 étages
pour simuler le bras :
 a) passer des consignes données aux muscles à l'activation des muscles. C'est
le rôle de la classe 'model/NeuroControl';
 b) passer de l'activation des muscles aux forces exercées sur les
articulations. C'est le rôle de la classe 'model/SimpleMuscle';
 c) passer des forces exercées sur les articulations aux mouvement effectifs
du bras. C'est le rôle de la classe 'model/Arm'.
Ces trois étapes sont mises en place dans 'model/CompleteArm'.

Trois examples très simples sont fournis.
 - 'example/ArmConsole' utilise juste une sortie console pour donner la position
du bras en fonction du temps, quand des consignes sont appliquées.
 - 'example/ArmGraphic' montre comment on peut faire une visualisation simple du
bras. Mais le bras n'est pas simulé, juste placé à différentes positions.
 - 'example/ExperienceConsole' utilise les consignes du fichier 'data/consigne_example.data'
pour simuler le bras et stocker toutes les variables dans le fichier
'data/result_example.data'.

Une application "complète" avec interface graphique permet d'expérimenter un peu
avec le bras ('gui/ExpGUI'). Lire les commentaires dans 'gui/ExpGui'.

Compiler et exécuter avec make
==============================
Ce qui suit suppose que les librairies dont dépend 'HumanArm' (voir section
suivante) sont dans le répertoire 'libs'. Si ce n'est pas le cas, il faut
s'adapter en conséquence.

Il suffit de se placer dans le répertoire HumanArm et de taper:
 - pour compiler : make class
 - pour lancer les exemples : make re ou make rc ou make rg ou make rgui
 
Dépendances
===========
 - v1.0+ : jama, vecmath
 - v1.5+ : jchart2s [http://jchart2d.sourceforge.net/]
 - v2.2+ : projet JavaUtils [git://github.com/snowgoon88/JavaUtils.git]

Nouveau dans la version 1.5
===========================
1) D'une part, on peut utiliser la classe 'model/CommandSequence' pour donner une
suite de consignes (model/Command) à un muscle. Ces consignes sont des paires
(temps, valeur). Ces consignes peuvent être lues et sauvegardées dans des fichiers.
Ainsi, le fichier 'data/consigne_example.data' contient la définition des consignes
pour le 6 muscles (utilisé par 'example/ExperienceConsole').

2) Les options du viewer/JArm2D peuvent être contrôlée par l'interface en utilisant
le JPanel de contrôle associé ('viewer/JArm2D.getControlPanel').
L'exemple 'example/ArmGraphic' met tout ceci en oeuvre.

3) Un nouvel exemple ('example/ExperienceConsole') où on utilise les CommandSequence
pour générer les consignes envoyées ensuite aux différents muscles. On y montre aussi
comment sauvegarder toutes les données de simulation.

4) Le Makfile de 'HumanArm' permet maintenant de lancer la compilation et les divers
exemples.

Nouveau dans la version 2.0
===========================
1) La classe JCommandSequence permet de modifier des consignes.

2) Avec gui/ExpGUI (et gui/JExperience) on a une interface graphique évoluée qui
permet de jouer avec le bras : définir des consignes (load/save), lancer des simulations
et enregistrer toutes les variables importantes dans un fichier.

Nouveau dans la version 2.2
===========================
1) Correction d'un bug qui permettait à l'activation de sortir de [0,1]
2) Peut afficher l'espace atteignable et un but du bras
3) Les contraintes du bras sont plus facilement utilisables
4) Un fichier 'ant'
5) On peut modifier les CommandSequence dans une Table sur le panneau de gauche
6) Le répertoire 'src.utils' n'est plus, et HumanArm fait maintenant appel
au Projet 'JavaUtils'. Cela n'est apparent que quand on 'fork' le projet. Les fichiers
'tgz' continuent d'inclure 'src.utils'.
 
La plupart des corrections/améliorations de cette version sont dues à
Thomas MOINEL.

A faire
=======
1) Générer une documentation.
 
Pour en savoir plus
===================
N'hésitez pas m'envoyer un petit mail : alain.dutech@loria.fr.



