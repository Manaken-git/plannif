### Contraintes manquantes pour un équivalent Lycée

Pour un emploi du temps complet et réaliste en lycée, plusieurs besoins métiers fréquents manquent actuellement :

#### **A. Équipement &amp; Adéquation des Salles**

* **Compatibilité Salle / Type de cours** : Vérifier que la salle attribuée possède les équipements nécessaires (ex. TP de Physique/Chimie dans un laboratoire, EPS dans un gymnase, TP informatique en salle informatique).
* **Capacité de la salle** : S'assurer que le nombre de places de la salle est supérieur ou égal au nombre d'élèves de la classe/groupe.

#### **B. Emploi du temps des Élèves**

* **Pause méridienne (Repas)** : Garantir un créneau libre pour le déjeuner (ex. au moins 1h30 entre 11h30 et 14h00) pour chaque classe.
* **Amplitude horaire quotidienne des élèves** : Limiter l'heure de début au plus tôt (ex. 8h00) et l'heure de fin au plus tard (ex. 17h00 ou 18h00), ainsi que le nombre total d'heures de cours par jour.
* **Minimisation des trous élèves** : Pénaliser les heures creuses au milieu de la journée de la classe.
* **Gestion des demi-groupes &amp; options (Alignements)** : Permettre de planifier simultanément deux demi-groupes d'une même classe (ex. TP SVT / TP Physique) ou d'aligner les options (ex. LV2) sur un même créneau pour plusieurs classes.

#### **C. Contraintes Enseignants Avancées**

* **Pause repas enseignant** : S'assurer qu'un professeur présent toute la journée dispose d'une pause déjeuner.
* **Indisponibilités par demi-journées / créneaux** : Gérer les vœux ou contraintes horaires précises (ex. indisponible le mardi matin) et non pas uniquement un jour complet (`dayOff`).
* **Temps de déplacement / Changement de bâtiment** : Éviter d'enchaîner deux cours consécutifs dans des bâtiments éloignés sans temps de transition.

#### **D. Organisation Pédagogique**

* **Matières à forte charge cognitive** : Privilégier le placement des matières principales (Maths, Français, etc.) le matin.
* **Séquençage des cours (Blocs de 2h)** : Permettre d'imposer ou de favoriser des créneaux doubles (2h d'affilée de la même matière).



###  Règles actuelles améliorables (Analyse technique &amp; Métier)

1. **Pondération dynamique des pénalités (** **teacherMaxHoursPerDay** **,** **teacherMaxHoursPerWeek** **,** **teacherMaxGap** **)**
  * *Problème* : Ces règles appliquent une pénalité fixe `HardSoftScore.ONE_SOFT`[2]. Un dépassement de 15 minutes ou de 5 heures produit exactement la même pénalité.
  * *Amélioration* : Pénaliser de façon proportionnelle au dépassement (ex. `penalize(HardSoftScore.ONE_SOFT, minutesExceeded)`).
2. **Logique durement codée ("hardcoded") dans** **seanceTypeDurationMatch**
  * *Problème* : La durée de 90 minutes est fixée en dur[2]. Si le type est `TP`, la condition `minutes != 90` autorise n'importe quelle durée hors 90 min (par exemple 10 minutes ou 5 heures)[2].
  * *Amélioration* : Rendre la durée attendue paramétrable selon le type de séance et vérifier une correspondance exacte (`minutes == dureeAttendue`).
3. **Nommage et logique de** **teacherClassMaxHoursConsecutive**
  * *Problème* : La méthode effectue la somme des heures sur deux jours consécutifs (`date.plusDays(1)`) pour un prof et une classe[2]. Elle ne mesure pas des heures *consécutives* au sens horaire dans une même journée.
  * *Amélioration* : Clarifier la règle (ex. `teacherClassMaxHoursOverTwoDays`) ou implémenter une vraie détection de cours consécutifs dans une même journée.
4. **Performance des calculs de durée (** **BigDecimal** **vs** **long** **)**
  * *Problème* : La méthode `getDurationInHours` convertit les durées en `BigDecimal` avec arrondi `HALF_UP` à chaque évaluation[2].
  * *Amélioration* : Effectuer les calculs et comparaisons directement en minutes (`long` via `ChronoUnit.MINUTES`), ce qui est beaucoup plus performant lors de l'exploration des solutions par Timefold.
5. **Utilisation d'une configuration de score (** **@ConstraintConfiguration** **)**
  * *Problème* : Toutes les pénalités sont figées à `ONE_HARD` ou `ONE_SOFT`[2].
  * *Amélioration* : Mettre en place la classe de configuration de score de Timefold (`@ConstraintConfiguration`) afin de permettre aux utilisateurs (proviseur, responsable planning) d'ajuster le poids de chaque contrainte depuis l'interface sans recompiler le code.