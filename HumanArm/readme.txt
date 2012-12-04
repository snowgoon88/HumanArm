HumanArm
--------

Quelques classes java pour simuler un bras humain très simple.

04/12/2012 - v1.0 - Fonctionnel avec examples console et graphic

Les bases
=========
Les commentaires dans les classes sont assez explicites.

La classe principale du modèle est 'model/CompleteArm'. Il y a en gros 3 étages pour simuler le bras :
 a) passer des consignes données aux muscles à l'activation des muscles. C'est le rôle de la classe 'model/NeuroControl';
 b) passer de l'activation des muscles aux forces exercées sur les articulations. C'est le rôle de la classe 'model/SimpleMuscle';
 c) passer des forces exercées sur les articulations aux mouvement effectifs du bras. C'est le rôle de la classe 'model/Arm'.
Ces trois étapes sont mise en place dans 'model/CompleteArm'.

Deux examples très simples sont fournis.
 - 'example/ArmConsole' utilise juste une sortie console pour donner la position du bras en fonction du temps, quand des consignes sont appliquées.
 - 'example/ArmGraphic' montre comment on peut faire une visualisation simple du bras. Mais le bras n'est pas simulé, juste placé à différentes positions.

Compiler et exécuter
====================
Aller dans 'src' et regarder le Makefile.

En cours de finition
====================
Dans la version future qui est presque finie, on aura :
 - un 'viewer/JArm2D' avec plus d'option, en particulier on pourra visualiser la trajectoire suivie par l'extrémité du bras.
 - un éditeur de consignes neuronales pour les 6 muscles
 - la visualisation de toutes les variables importantes
 - la sauvegarde de ces variables dans un fichier texte (ce qui permet de faire des analyse et autre).

Pour en savoir plus
===================
N'hésitez pas m'envoyer un petit mail : alain.dutech@loria.fr.



