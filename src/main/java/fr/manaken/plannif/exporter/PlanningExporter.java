package fr.manaken.plannif.exporter;

import fr.manaken.plannif.business.Planning;
import fr.manaken.plannif.model.Seance;
import fr.manaken.plannif.model.Professeur;

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
        html.append("<meta charset=\"UTF-8\">\n<title>Planning - Vue Gantt</title>\n");
        appendStyles(html);
        html.append("</head>\n<body>\n");

        html.append("<header>\n");
        html.append("  <h1>Planning Universitaire</h1>\n");
        html.append("  <div class=\"score\">Score: ").append(planning.getScore()).append("</div>\n");
        html.append("</header>\n");

        html.append("<main>\n");

        // Regrouper les séances par jour puis par professeur
        Map<String, Map<Professeur, List<Seance>>> sessionsByDayAndProf = planning.getSeances().stream()
                .filter(s -> s.getCreneau() != null)
                .collect(Collectors.groupingBy(
                        s -> s.getCreneau().getDebut().toLocalDate().toString(),
                        TreeMap::new,
                        Collectors.groupingBy(Seance::getProfesseur)
                ));

        for (String dayStr : sessionsByDayAndProf.keySet()) {
            java.time.LocalDate date = java.time.LocalDate.parse(dayStr);
            html.append("<section class=\"day-section\">\n");
            html.append("  <h2 class=\"day-title\">").append(date.format(DATE_FORMATTER)).append("</h2>\n");
            
            html.append("  <div class=\"timeline-container\">\n");
            html.append("    <div class=\"timeline-header\">\n");
            html.append("      <div class=\"row-label\">Professeur</div>\n");
            for (int h = 8; h <= 18; h++) {
                html.append("      <div class=\"time-slot\">").append(h).append("h</div>\n");
            }
            html.append("    </div>\n");

            Map<Professeur, List<Seance>> profSessions = sessionsByDayAndProf.get(dayStr);
            for (Professeur prof : profSessions.keySet()) {
                html.append("    <div class=\"timeline-row\">\n");
                html.append("      <div class=\"row-label\">").append(prof.getNom()).append("</div>\n");
                html.append("      <div class=\"row-content\">\n");
                
                // Ajouter des lignes de grille
                for (int h = 8; h <= 18; h++) {
                    html.append("        <div class=\"grid-line\"></div>\n");
                }

                for (Seance seance : profSessions.get(prof)) {
                    double startOffset = calculateOffset(seance.getCreneau().getDebut());
                    double width = calculateWidth(seance.getCreneau().getDebut(), seance.getCreneau().getFin());
                    
                    html.append("        <div class=\"session-bar\" style=\"left: ")
                            .append(startOffset).append("%; width: ").append(width).append("%;\" ")
                            .append("title=\"").append(seance.getMatiere().getNom()).append(" - ").append(seance.getClasse().getNom()).append("\">\n");
                    html.append("          <div class=\"session-title\">").append(seance.getMatiere().getNom()).append("</div>\n");
                    html.append("          <div class=\"session-info\">")
                            .append(seance.getClasse().getNom()).append(" - ")
                            .append(seance.getSalle() != null ? seance.getSalle().getCode() : "N/A")
                            .append("</div>\n");
                    html.append("          <div class=\"session-time\">")
                            .append(seance.getCreneau().getDebut().format(TIME_FORMATTER))
                            .append(" - ")
                            .append(seance.getCreneau().getFin().format(TIME_FORMATTER))
                            .append("</div>\n");
                    html.append("        </div>\n");
                }
                html.append("      </div>\n");
                html.append("    </div>\n");
            }
            html.append("  </div>\n");
            html.append("</section>\n");
        }

        html.append("</main>\n");
        html.append("</body>\n</html>");

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(html.toString());
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de l'export HTML", e);
        }
    }

    private static double calculateOffset(java.time.LocalDateTime start) {
        double startHour = start.getHour() + (start.getMinute() / 60.0);
        return ((startHour - 8.0) / 11.0) * 100.0;
    }

    private static double calculateWidth(java.time.LocalDateTime start, java.time.LocalDateTime end) {
        long minutes = ChronoUnit.MINUTES.between(start, end);
        return (minutes / (11.0 * 60.0)) * 100.0;
    }

    private static void appendStyles(StringBuilder html) {
        html.append("<style>\n");
        html.append("  :root {\n");
        html.append("    --primary: #2563eb;\n");
        html.append("    --secondary: #64748b;\n");
        html.append("    --bg: #f8fafc;\n");
        html.append("    --card: #ffffff;\n");
        html.append("    --text: #1e293b;\n");
        html.append("    --border: #e2e8f0;\n");
        html.append("  }\n");
        html.append("  body { font-family: 'Inter', system-ui, sans-serif; background: var(--bg); color: var(--text); margin: 0; padding: 20px; }\n");
        html.append("  header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 30px; border-bottom: 2px solid var(--border); padding-bottom: 15px; }\n");
        html.append("  h1 { margin: 0; color: var(--primary); }\n");
        html.append("  .score { font-weight: bold; background: #dcfce7; color: #166534; padding: 8px 16px; border-radius: 20px; }\n");
        html.append("  .day-section { background: var(--card); border-radius: 12px; box-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1); padding: 20px; margin-bottom: 40px; }\n");
        html.append("  .day-title { margin-top: 0; padding-bottom: 10px; border-bottom: 1px solid var(--border); text-transform: capitalize; }\n");
        html.append("  .timeline-container { position: relative; margin-top: 20px; border: 1px solid var(--border); border-radius: 8px; overflow: hidden; }\n");
        html.append("  .timeline-header { display: flex; background: #f1f5f9; border-bottom: 1px solid var(--border); }\n");
        html.append("  .timeline-row { display: flex; border-bottom: 1px solid var(--border); min-height: 80px; }\n");
        html.append("  .row-label { width: 150px; padding: 10px; font-weight: bold; border-right: 1px solid var(--border); flex-shrink: 0; display: flex; align-items: center; background: #fafafa; }\n");
        html.append("  .time-slot { flex: 1; padding: 10px; text-align: center; border-right: 1px solid var(--border); font-size: 0.85em; color: var(--secondary); }\n");
        html.append("  .row-content { flex: 1; position: relative; display: flex; }\n");
        html.append("  .grid-line { flex: 1; border-right: 1px solid #f0f0f0; height: 100%; pointer-events: none; }\n");
        html.append("  .session-bar { position: absolute; height: 80%; top: 10%; background: var(--primary); color: white; border-radius: 6px; padding: 8px; font-size: 0.75em; overflow: hidden; box-shadow: 0 2px 4px rgba(0,0,0,0.1); transition: transform 0.2s; cursor: help; }\n");
        html.append("  .session-bar:hover { transform: scale(1.02); z-index: 10; }\n");
        html.append("  .session-title { font-weight: bold; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }\n");
        html.append("  .session-info { opacity: 0.9; margin: 2px 0; }\n");
        html.append("  .session-time { font-style: italic; font-size: 0.9em; }\n");
        html.append("</style>\n");
    }
}
