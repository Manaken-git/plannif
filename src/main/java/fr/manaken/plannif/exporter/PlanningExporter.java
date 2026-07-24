package fr.manaken.plannif.exporter;

import fr.manaken.plannif.business.Planning;
import fr.manaken.plannif.model.Seance;

import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class PlanningExporter {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("EEEE d MMMM");

    public static void exportToHtml(Planning planning, String filePath) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>\n<html lang=\"fr\">\n<head>\n");
        html.append("<meta charset=\"UTF-8\">\n<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("<title>Planning - Dashboard Interactif</title>\n");
        appendStyles(html);
        html.append("</head>\n<body>\n");

        // Stats calculation
        Map<fr.manaken.plannif.model.Professeur, Double> hoursByProf = new HashMap<>();
        Map<fr.manaken.plannif.model.Classe, Double> hoursByClasse = new HashMap<>();
        Map<fr.manaken.plannif.model.Matiere, Double> hoursByMatiere = new HashMap<>();

        for (Seance s : planning.getSeances()) {
            if (s.getCreneau() != null && s.getProfesseur() != null) {
                double duration = ChronoUnit.MINUTES.between(s.getCreneau().getDebut(), s.getCreneau().getFin()) / 60.0;
                hoursByProf.merge(s.getProfesseur(), duration, (v1, v2) -> v1 + v2);
                hoursByClasse.merge(s.getClasse(), duration, (v1, v2) -> v1 + v2);
                hoursByMatiere.merge(s.getMatiere(), duration, (v1, v2) -> v1 + v2);
            }
        }

        html.append("<div class=\"app-container\">\n");
        
        // SIDEBAR FILTERS
        html.append("  <aside class=\"sidebar\">\n");
        html.append("    <h2>Filtres</h2>\n");
        
        appendFilterSection(html, "Professeurs", planning.getProfesseurs(), "prof");
        appendFilterSection(html, "Classes", planning.getClasses(), "classe");
        appendFilterSection(html, "Matières", planning.getMatieres(), "matiere");
        
        html.append("  </aside>\n");

        // MAIN CONTENT
        html.append("  <main class=\"main-content\">\n");
        html.append("    <header>\n");
        html.append("      <h1>Tableau de Bord du Planning</h1>\n");
        html.append("      <div class=\"score\">Score: ").append(planning.getScore()).append("</div>\n");
        html.append("    </header>\n");

        // SUMMARY STATS
        html.append("    <section class=\"stats-grid\">\n");
        appendStatsCard(html, "Alerte Professeurs", hoursByProf, "prof");
        appendStatsCard(html, "Charge Classes", hoursByClasse, "classe");
        appendStatsCard(html, "Volume Matières", hoursByMatiere, "matiere");
        appendConfigCard(html, "Calendrier des Modules", planning.getMatiereClasseConfigs());
        html.append("    </section>\n");

        // GANTT VIEW
        Map<String, Map<fr.manaken.plannif.model.Professeur, List<Seance>>> sessionsByDayAndProf = planning.getSeances().stream()
                .filter(s -> s.getCreneau() != null && s.getProfesseur() != null)
                .collect(Collectors.groupingBy(
                        s -> "Planning Général",
                        TreeMap::new,
                        Collectors.groupingBy(Seance::getProfesseur)
                ));

        for (String dayStr : sessionsByDayAndProf.keySet()) {
            html.append("    <section class=\"day-section\">\n");
            html.append("      <h2 class=\"day-title\">").append(dayStr).append("</h2>\n");
            html.append("      <div class=\"timeline-container\">\n");
            html.append("        <div class=\"timeline-header\"><div class=\"row-label\">Professeur</div>");
            for (int h = 8; h <= 18; h++) html.append("<div class=\"time-slot\">").append(h).append("h</div>");
            html.append("</div>\n");

            Map<fr.manaken.plannif.model.Professeur, List<Seance>> profSessions = sessionsByDayAndProf.get(dayStr);
            for (fr.manaken.plannif.model.Professeur prof : profSessions.keySet()) {
                html.append("        <div class=\"timeline-row\" data-prof-id=\"").append(prof.getId()).append("\">\n");
                html.append("          <div class=\"row-label\">").append(prof.getNom()).append("</div>\n");
                html.append("          <div class=\"row-content\">\n");
                for (int h = 8; h <= 18; h++) html.append("<div class=\"grid-line\"></div>");
                
                for (Seance seance : profSessions.get(prof)) {
                    double offset = calculateOffset(seance.getCreneau().getDebut().toLocalTime());
                    double width = calculateWidth(seance.getCreneau().getDebut().toLocalTime(), seance.getCreneau().getFin().toLocalTime());
                    html.append("            <div class=\"session-bar\" style=\"left: ").append(offset).append("%; width: ").append(width).append("%;\" ")
                            .append("data-prof-id=\"").append(prof.getId()).append("\" ")
                            .append("data-classe-id=\"").append(seance.getClasse().getId()).append("\" ")
                            .append("data-matiere-id=\"").append(seance.getMatiere().getId()).append("\" ")
                            .append("title=\"").append(seance.getMatiere().getNom()).append(" - ").append(seance.getClasse().getNom()).append("\">\n")
                            .append("              <div class=\"session-title\">").append(seance.getMatiere().getNom()).append("</div>\n")
                            .append("              <div class=\"session-info\">").append(seance.getClasse().getNom()).append("</div>\n")
                            .append("              <div class=\"session-time\">").append(seance.getCreneau().getDebut().format(TIME_FORMATTER)).append("</div>\n")
                            .append("            </div>\n");
                }
                html.append("          </div>\n        </div>\n");
            }
            html.append("      </div>\n    </section>\n");
        }

        html.append("  </main>\n");
        html.append("</div>\n");
        
        appendScripts(html);
        html.append("</body>\n</html>");

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(html.toString());
        } catch (IOException e) {
            throw new RuntimeException("Erreur export", e);
        }
    }

    private static void appendFilterSection(StringBuilder html, String title, Collection<?> items, String type) {
        html.append("    <div class=\"filter-group\">\n      <h3>").append(title).append("</h3>\n");
        html.append("      <div class=\"filter-list\">\n");
        html.append("        <label class=\"select-all-label\"><input type=\"checkbox\" checked class=\"select-all\" onchange=\"toggleAll('").append(type).append("', this.checked)\"> <b>Tout sélectionner</b></label>\n");
        for (Object item : items) {
            Long id = 0L;
            String name = "";
            if (item instanceof fr.manaken.plannif.model.Professeur) { id = ((fr.manaken.plannif.model.Professeur)item).getId(); name = ((fr.manaken.plannif.model.Professeur)item).getNom(); }
            else if (item instanceof fr.manaken.plannif.model.Classe) { id = ((fr.manaken.plannif.model.Classe)item).getId(); name = ((fr.manaken.plannif.model.Classe)item).getNom(); }
            else if (item instanceof fr.manaken.plannif.model.Matiere) { id = ((fr.manaken.plannif.model.Matiere)item).getId(); name = ((fr.manaken.plannif.model.Matiere)item).getNom(); }
            
            html.append("        <label><input type=\"checkbox\" checked onchange=\"filterData()\" data-type=\"").append(type).append("\" data-id=\"").append(id).append("\"> ").append(name).append("</label>\n");
        }
        html.append("      </div>\n    </div>\n");
    }

    private static void appendStatsCard(StringBuilder html, String title, Map<?, Double> data, String type) {
        html.append("      <div class=\"stats-card\">\n        <h3>").append(title).append("</h3>\n");
        html.append("        <table>\n          <thead><tr><th>Nom</th><th>Heures</th><th>Statut</th></tr></thead>\n          <tbody>\n");
        for (Map.Entry<?, Double> entry : data.entrySet()) {
            String name = "";
            double target = 0.0;
            if (entry.getKey() instanceof fr.manaken.plannif.model.Professeur) { 
                name = ((fr.manaken.plannif.model.Professeur)entry.getKey()).getNom(); 
                java.math.BigDecimal nb = ((fr.manaken.plannif.model.Professeur)entry.getKey()).getNb_heures();
                if (nb != null) target = nb.doubleValue();
            } else if (entry.getKey() instanceof fr.manaken.plannif.model.Matiere) {
                name = ((fr.manaken.plannif.model.Matiere)entry.getKey()).getNom();
                Long vol = ((fr.manaken.plannif.model.Matiere)entry.getKey()).getVolumeHoraireAnnuel();
                if (vol != null) target = vol.doubleValue();
            } else if (entry.getKey() instanceof fr.manaken.plannif.model.Classe) {
                name = ((fr.manaken.plannif.model.Classe)entry.getKey()).getNom();
                target = -1; // No target for class in basic model
            }
            
            double hours = entry.getValue();
            String status = "OK";
            String badgeClass = "badge-ok";
            if (target > 0) {
                if (hours > target) { status = "SURCHGE"; badgeClass = "badge-err"; }
                else if (hours < target * 0.5) { status = "SOUSCHGE"; badgeClass = "badge-warn"; }
            }
            
            html.append("            <tr><td>").append(name).append("</td><td>").append(String.format("%.1f", hours)).append("h</td>")
                .append("<td><span class=\"badge ").append(badgeClass).append("\">").append(status).append("</span></td></tr>\n");
        }
        html.append("          </tbody>\n        </table>\n      </div>\n");
    }

    private static void appendConfigCard(StringBuilder html, String title, List<fr.manaken.plannif.model.MatiereClasseConfig> configs) {
        html.append("      <div class=\"stats-card\">\n        <h3>").append(title).append("</h3>\n");
        if (configs == null || configs.isEmpty()) {
            html.append("        <p style=\"font-size: 0.85rem; color: var(--secondary);\">Aucune contrainte de période définie.</p>\n");
        } else {
            html.append("        <table>\n          <thead><tr><th>Classe</th><th>Matière</th><th>Période</th></tr></thead>\n          <tbody>\n");
            for (fr.manaken.plannif.model.MatiereClasseConfig config : configs) {
                if (config.getClasse() == null || config.getMatiere() == null) continue;
                String period = (config.getDateDebut() != null ? config.getDateDebut() : "...") + " au " + (config.getDateFin() != null ? config.getDateFin() : "...");
                html.append("            <tr><td>").append(config.getClasse().getNom()).append("</td>")
                    .append("<td>").append(config.getMatiere().getNom()).append("</td>")
                    .append("<td><span class=\"badge badge-ok\" style=\"font-size: 0.7rem;\">").append(period).append("</span></td></tr>\n");
            }
            html.append("          </tbody>\n        </table>\n");
        }
        html.append("      </div>\n");
    }

    private static void appendScripts(StringBuilder html) {
        html.append("<script>\n");
        html.append("function toggleAll(type, checked) {\n");
        html.append("  const checkboxes = document.querySelectorAll('input[data-type=\"' + type + '\"]');\n");
        html.append("  checkboxes.forEach(cb => { cb.checked = checked; });\n");
        html.append("  filterData();\n");
        html.append("}\n\n");
        
        html.append("function filterData() {\n");
        html.append("  const checkboxes = document.querySelectorAll('input[type=\"checkbox\"]:not(.select-all)');\n");
        html.append("  const filters = { prof: [], classe: [], matiere: [] };\n");
        html.append("  checkboxes.forEach(cb => { if(cb.checked) filters[cb.dataset.type].push(cb.dataset.id); });\n\n");
        
        html.append("  const bars = document.querySelectorAll('.session-bar');\n");
        html.append("  bars.forEach(bar => {\n");
        html.append("    const show = filters.prof.includes(bar.dataset.profId) &&\n");
        html.append("                 filters.classe.includes(bar.dataset.classeId) &&\n");
        html.append("                 filters.matiere.includes(bar.dataset.matiereId);\n");
        html.append("    bar.style.display = show ? 'block' : 'none';\n");
        html.append("  });\n\n");
        
        html.append("  document.querySelectorAll('.timeline-row').forEach(row => {\n");
        html.append("    const profId = row.dataset.profId;\n");
        html.append("    const hasVisibleSessions = Array.from(row.querySelectorAll('.session-bar')).some(bar => bar.style.display !== 'none');\n");
        html.append("    row.style.display = (filters.prof.includes(profId) && (hasVisibleSessions || filters.prof.length === 1)) ? 'flex' : 'none';\n");
        html.append("    // Simple logic: if prof is selected, show row.\n");
        html.append("    row.style.display = filters.prof.includes(profId) ? 'flex' : 'none';\n");
        html.append("  });\n");
        html.append("}\n");
        html.append("</script>\n");
    }

    private static double calculateOffset(java.time.LocalTime start) {
        double startHour = start.getHour() + (start.getMinute() / 60.0);
        return ((startHour - 8.0) / 11.0) * 100.0;
    }

    private static double calculateWidth(java.time.LocalTime start, java.time.LocalTime end) {
        long minutes = ChronoUnit.MINUTES.between(start, end);
        return (minutes / (11.0 * 60.0)) * 100.0;
    }

    private static void appendStyles(StringBuilder html) {
        html.append("<style>\n");
        html.append("  :root { --primary: #2563eb; --secondary: #64748b; --bg: #f8fafc; --card: #ffffff; --text: #1e293b; --border: #e2e8f0; --accent: #8b5cf6; }\n");
        html.append("  body { font-family: 'Outfit', sans-serif; background: var(--bg); color: var(--text); margin: 0; }\n");
        html.append("  .app-container { display: flex; min-height: 100vh; }\n");
        
        html.append("  /* SIDEBAR */\n");
        html.append("  .sidebar { width: 280px; background: var(--card); border-right: 1px solid var(--border); padding: 25px; position: sticky; top: 0; height: 100vh; overflow-y: auto; }\n");
        html.append("  .sidebar h2 { color: var(--primary); font-size: 1.5rem; margin-bottom: 30px; }\n");
        html.append("  .filter-group { margin-bottom: 25px; }\n");
        html.append("  .filter-group h3 { font-size: 0.9rem; text-transform: uppercase; color: var(--secondary); margin-bottom: 12px; letter-spacing: 0.05em; }\n");
        html.append("  .filter-list { display: flex; flex-direction: column; gap: 8px; }\n");
        html.append("  .filter-list label { display: flex; align-items: center; gap: 10px; font-size: 0.9rem; cursor: pointer; padding: 4px 8px; border-radius: 6px; transition: background 0.2s; }\n");
        html.append("  .filter-list label:hover { background: #f1f5f9; }\n");
        html.append("  .select-all-label { border-bottom: 1px solid var(--border); margin-bottom: 5px; padding-bottom: 8px !important; }\n");
        
        html.append("  /* MAIN CONTENT */\n");
        html.append("  .main-content { flex: 1; padding: 40px; overflow-y: auto; }\n");
        html.append("  header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; }\n");
        html.append("  h1 { font-size: 2rem; margin: 0; background: linear-gradient(to right, var(--primary), var(--accent)); -webkit-background-clip: text; -webkit-text-fill-color: transparent; }\n");
        html.append("  .score { font-weight: bold; background: #dcfce7; color: #166534; padding: 10px 20px; border-radius: 30px; box-shadow: 0 2px 4px rgba(0,0,0,0.05); }\n");

        html.append("  /* STATS CARDS */\n");
        html.append("  .stats-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 20px; margin-bottom: 40px; }\n");
        html.append("  @media (min-width: 1400px) { .stats-grid { grid-template-columns: repeat(4, 1fr); } }\n");
        html.append("  .stats-card { background: var(--card); border-radius: 12px; padding: 20px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1); border: 1px solid var(--border); }\n");
        html.append("  .stats-card h3 { margin: 0 0 15px 0; font-size: 1.1rem; color: var(--secondary); }\n");
        html.append("  .stats-card table { width: 100%; border-collapse: collapse; font-size: 0.85rem; }\n");
        html.append("  .stats-card th { text-align: left; padding: 8px; border-bottom: 2px solid var(--border); color: var(--secondary); }\n");
        html.append("  .stats-card td { padding: 8px; border-bottom: 1px solid var(--border); }\n");
        html.append("  .badge { padding: 2px 8px; border-radius: 12px; font-size: 0.75rem; font-weight: bold; }\n");
        html.append("  .badge-ok { background: #dcfce7; color: #166534; }\n");
        html.append("  .badge-warn { background: #fef9c3; color: #854d0e; }\n");
        html.append("  .badge-err { background: #fee2e2; color: #991b1b; }\n");

        html.append("  /* GANTT */\n");
        html.append("  .day-section { background: var(--card); border-radius: 16px; padding: 25px; margin-bottom: 40px; box-shadow: 0 10px 15px -3px rgba(0,0,0,0.1); border: 1px solid var(--border); }\n");
        html.append("  .day-title { margin-top: 0; padding-bottom: 15px; border-bottom: 2px solid var(--border); text-transform: capitalize; color: var(--primary); }\n");
        html.append("  .timeline-container { margin-top: 20px; border: 1px solid var(--border); border-radius: 10px; overflow-x: auto; }\n");
        html.append("  .timeline-header { display: flex; background: #f1f5f9; border-bottom: 1px solid var(--border); min-width: 1000px; }\n");
        html.append("  .timeline-row { display: flex; border-bottom: 1px solid var(--border); min-height: 100px; min-width: 1000px; }\n");
        html.append("  .row-label { width: 180px; padding: 15px; font-weight: 600; border-right: 1px solid var(--border); background: #fafafa; display: flex; align-items: center; flex-shrink: 0; }\n");
        html.append("  .time-slot { flex: 1; padding: 12px; text-align: center; border-right: 1px solid var(--border); font-size: 0.8rem; font-weight: bold; color: var(--secondary); }\n");
        html.append("  .row-content { flex: 1; position: relative; display: flex; }\n");
        html.append("  .grid-line { flex: 1; border-right: 1px solid #f1f5f9; height: 100%; pointer-events: none; }\n");
        html.append("  .session-bar { position: absolute; height: 80%; top: 10%; background: linear-gradient(135deg, var(--primary), var(--accent)); color: white; border-radius: 8px; padding: 12px; font-size: 0.8rem; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1); transition: all 0.3s; cursor: pointer; border: 1px solid rgba(255,255,255,0.2); }\n");
        html.append("  .session-bar:hover { transform: translateY(-3px) scale(1.01); z-index: 50; box-shadow: 0 10px 15px -3px rgba(0,0,0,0.2); }\n");
        html.append("  .session-title { font-weight: bold; font-size: 0.9rem; margin-bottom: 4px; }\n");
        html.append("  .session-time { font-family: monospace; opacity: 0.9; margin-top: 8px; display: block; }\n");
        html.append("</style>\n");
    }

}
