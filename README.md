# ArchitectureDistribueeTP5
Architecture distribuée TP 5

BERNIERE Vincent
BRODIER Quentin
Date : 31 Mars 2016


Au lancement du programme, voici les options disponibles :

all : Retourne toutes les villes
search [nomVille] : Retourne toutes les villes avec comme nom [nomVille]
geonames [nomVille] : Retourne toutes les villes via Geonames avec comme nom [nomVille]
jgroups [message] : Envoie un [message] au cluster m1gil
exit : Stop le programme


Attention, pensez à bien démarrer le service Rest du TP1 en parallèle pour effectuer
les recherches des villes (all & search).