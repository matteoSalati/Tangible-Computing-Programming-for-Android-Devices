Legenda codice comandi per Blockly:

Avanti --> 1
Sinistra --> 2
Destra --> 3
While --> 10
Chiusura While --> 5
If --> Tre diverse possibilità:
					If Avanti (formato da comando If seguito dal comando Avanti) --> 8
					If Sinistra (formato da comando If seguito dal comando Sinistra) --> 6
					If Destra (formato da comando If seguito dal comando Destra) --> 7
Chiusura If --> 9
Else --> 4 ----> NB. Da usare dopo If, ma senza chiudere If stesso!!
					 Es: If Avanti allora Avanti
						 Else Sinistra
						 
						[8, 1, 4, 2, 9] <-- OK
						[8, 1, 9, 4, 2, 9] <-- SBAGLIATO
