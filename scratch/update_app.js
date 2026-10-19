const fs = require('fs');
const path = require('path');

const appPath = path.resolve('E:/Boulot/workspace/planning-plannif-vue/src/App.tsx');
let app = fs.readFileSync(appPath, 'utf8');

const targetBeforeEnd = `      {/* SCORE BREAKDOWN & AUDIT MODAL */}
      <ScoreBreakdownModal
        isOpen={isScoreModalOpen}
        onClose={() => setIsScoreModalOpen(false)}
        planning={planningData}
        scoreExplanation={scoreExplanation}
      />`;

const solverModal = `      {/* SCORE BREAKDOWN & AUDIT MODAL */}
      <ScoreBreakdownModal
        isOpen={isScoreModalOpen}
        onClose={() => setIsScoreModalOpen(false)}
        planning={planningData}
        scoreExplanation={scoreExplanation}
      />

      {/* SOLVER SSE PROGRESS MODAL */}
      {currentJobId && isPlanning && (
        <div className="solver-modal-overlay">
          <div className="solver-modal-container">
            <div className="solver-icon-wrapper">
              <span className="solver-pulse-ring"></span>
              ⚡
            </div>
            <h3 className="solver-title">Optimisation d'Emploi du Temps</h3>
            <p className="solver-subtitle">
              Timefold Solver explore les permutations (Statut : {jobStatus || 'SOLVING'})...
            </p>

            <div className="solver-timer-badge">
              ⏱️ Temps écoulé : {jobElapsedSeconds}s
            </div>

            <div className="solver-metrics-grid">
              <div className="solver-metric-card">
                <div className="solver-metric-label">Conflits Durs (Faisabilité)</div>
                <div className={\`solver-metric-value \${jobScore && jobScore.startsWith('0hard') ? 'hard-ok' : 'hard-ko'}\`}>
                  {jobScore ? (jobScore.split('/')[0] || 'Calcul...') : 'Calcul...'}
                </div>
              </div>
              <div className="solver-metric-card">
                <div className="solver-metric-label">Score Souple (Confort)</div>
                <div className="solver-metric-value soft">
                  {jobScore ? (jobScore.split('/')[1] || 'En cours...') : 'En cours...'}
                </div>
              </div>
            </div>

            <div className="solver-note">
              Les séances épinglées (🔒) sont strictement préservées. La sauvegarde s'effectue automatiquement en base dès la fin de l'optimisation.
            </div>

            <div className="solver-actions">
              <button
                type="button"
                className="btn-stop-solver"
                onClick={handleStopPlanningJob}
                disabled={isStoppingJob}
              >
                🛑 {isStoppingJob ? 'Arrêt en cours...' : 'Arrêter et figer la meilleure solution'}
              </button>
            </div>
          </div>
        </div>
      )}`;

if (!app.includes('SOLVER SSE PROGRESS MODAL')) {
  app = app.replace(targetBeforeEnd, solverModal);
}

fs.writeFileSync(appPath, app, 'utf8');
console.log('App.tsx modal inserted.');
