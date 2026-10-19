const fs = require('fs');
const path = require('path');

const vueRoot = path.resolve('E:/Boulot/workspace/planning-plannif-vue');

console.log('Vue root:', vueRoot);

// 1. Update WeekGrid.tsx
const weekGridPath = path.join(vueRoot, 'src/components/organisms/WeekGrid.tsx');
let weekGrid = fs.readFileSync(weekGridPath, 'utf8');

if (!weekGrid.includes('onTogglePin?:')) {
  weekGrid = weekGrid.replace(
    'indisponibilites?: Indisponibilite[];\n}',
    'indisponibilites?: Indisponibilite[];\n  onTogglePin?: (seanceId: number) => void;\n}'
  );
  weekGrid = weekGrid.replace(
    'indisponibilites = [],\n}) => {',
    'indisponibilites = [],\n  onTogglePin,\n}) => {'
  );
  weekGrid = weekGrid.replace(
    `<div className="week-session-time">\n                          🕒 {startTime} - {endTime}\n                          {isDoubleBlock && <span className="session-badge-bloc">Bloc 2h</span>}\n                          {seance.type === 'TP' && <span className="session-badge-tp">TP</span>}\n                        </div>`,
    `<div className="week-session-time">\n                          <span>🕒 {startTime} - {endTime}</span>\n                          <div className="week-session-badges">\n                            {isDoubleBlock && <span className="session-badge-bloc">Bloc 2h</span>}\n                            {seance.type === 'TP' && <span className="session-badge-tp">TP</span>}\n                            <button type="button" className={\`session-pin-btn \${seance.pinned ? 'pinned' : ''}\`} onClick={(e) => { e.stopPropagation(); onTogglePin?.(seance.id); }} title={seance.pinned ? 'Séance imposée (verrouillée) - Cliquer pour déverrouiller' : 'Cliquer pour verrouiller cette séance'}>{seance.pinned ? '🔒' : '🔓'}</button>\n                          </div>\n                        </div>`
  );
  fs.writeFileSync(weekGridPath, weekGrid, 'utf8');
  console.log('WeekGrid.tsx updated.');
}

// 2. Update WeekGrid.css
const weekGridCssPath = path.join(vueRoot, 'src/components/organisms/WeekGrid.css');
let weekGridCss = fs.readFileSync(weekGridCssPath, 'utf8');
if (!weekGridCss.includes('.session-pin-btn')) {
  weekGridCss += `\n
.week-session-badges {
  display: flex;
  align-items: center;
  gap: 4px;
}

.session-pin-btn {
  background: transparent;
  border: none;
  cursor: pointer;
  font-size: 0.85rem;
  padding: 1px 4px;
  border-radius: 4px;
  transition: transform 0.15s ease, background-color 0.15s ease;
  opacity: 0.55;
}

.session-pin-btn:hover {
  transform: scale(1.2);
  opacity: 1;
  background: rgba(0, 0, 0, 0.05);
}

.session-pin-btn.pinned {
  opacity: 1;
  background: #fef3c7;
  border: 1px solid #fde68a;
}
`;
  fs.writeFileSync(weekGridCssPath, weekGridCss, 'utf8');
  console.log('WeekGrid.css updated.');
}

// 3. Update MonthGrid.tsx
const monthGridPath = path.join(vueRoot, 'src/components/organisms/MonthGrid.tsx');
let monthGrid = fs.readFileSync(monthGridPath, 'utf8');
if (!monthGrid.includes('onTogglePin?:')) {
  monthGrid = monthGrid.replace(
    'selectedMatieres: number[];\n}',
    'selectedMatieres: number[];\n  onTogglePin?: (seanceId: number) => void;\n}'
  );
  monthGrid = monthGrid.replace(
    'selectedMatieres\n}) => {',
    'selectedMatieres,\n  onTogglePin\n}) => {'
  );
  monthGrid = monthGrid.replace(
    `<span className="pill-time">{time}</span>`,
    `<span className="pill-time">{time} {s.pinned && '🔒'}</span>`
  );
  monthGrid = monthGrid.replace(
    `<div className="card-time-slot">🕒 {start} - {end}</div>`,
    `<div className="card-time-slot" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}><span>🕒 {start} - {end}</span><button type="button" className={\`session-pin-btn \${seance.pinned ? 'pinned' : ''}\`} onClick={(e) => { e.stopPropagation(); onTogglePin?.(seance.id); }} title={seance.pinned ? 'Séance imposée (verrouillée) - Cliquer pour déverrouiller' : 'Cliquer pour verrouiller cette séance'}>{seance.pinned ? '🔒' : '🔓'}</button></div>`
  );
  fs.writeFileSync(monthGridPath, monthGrid, 'utf8');
  console.log('MonthGrid.tsx updated.');
}

// 4. Update ConstraintSettingsModal.tsx
const constraintModalPath = path.join(vueRoot, 'src/components/organisms/ConstraintSettingsModal.tsx');
let constraintModal = fs.readFileSync(constraintModalPath, 'utf8');
if (!constraintModal.includes('studentDayStartTimeWeight')) {
  const targetSnippet = `{renderSlider(
                'pauseMerdienneEleveWeight',
                'Pause méridienne Élèves (Repas)',
                'Garantir au moins 1h30 sans cours entre 11h30 et 14h00 pour chaque classe.',
                'Soft'
              )}`;
  const replacementSnippet = `{renderSlider(
                'studentDayStartTimeWeight',
                'Verrouillage début des cours à 8h00 (9h lundi)',
                'Pénalise proportionnellement au retard tout premier cours débutant après 8h00 (ou 9h00 le lundi) pour compacter la journée des élèves.',
                'Soft'
              )}\n              ` + targetSnippet;
  constraintModal = constraintModal.replace(targetSnippet, replacementSnippet);
  fs.writeFileSync(constraintModalPath, constraintModal, 'utf8');
  console.log('ConstraintSettingsModal.tsx updated.');
}

// 5. Update App.css
const appCssPath = path.join(vueRoot, 'src/App.css');
let appCss = fs.readFileSync(appCssPath, 'utf8');
if (!appCss.includes('.solver-modal-overlay')) {
  appCss += `\n
/* Modal de Progression Solver (SSE) */
.solver-modal-overlay {
  position: fixed;
  inset: 0;
  background-color: rgba(15, 23, 42, 0.7);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: solverFadeIn 0.2s ease-out;
}

.solver-modal-container {
  background: var(--card, #ffffff);
  border-radius: 16px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.15), 0 10px 10px -5px rgba(0, 0, 0, 0.05);
  width: 90%;
  max-width: 520px;
  padding: 30px;
  text-align: center;
  border: 1px solid var(--border, #e2e8f0);
  position: relative;
}

.solver-icon-wrapper {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: #eff6ff;
  color: #2563eb;
  margin-bottom: 16px;
  font-size: 32px;
  position: relative;
}

.solver-pulse-ring {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  border: 3px solid #3b82f6;
  opacity: 0.6;
  animation: solverPulse 1.8s cubic-bezier(0, 0, 0.2, 1) infinite;
}

.solver-title {
  font-size: 1.3rem;
  font-weight: 700;
  color: var(--text, #1e293b);
  margin: 0 0 6px 0;
}

.solver-subtitle {
  font-size: 0.88rem;
  color: var(--text-muted, #64748b);
  margin: 0 0 18px 0;
}

.solver-metrics-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin: 18px 0;
}

.solver-metric-card {
  background: rgba(0, 0, 0, 0.02);
  border: 1px solid var(--border, #e2e8f0);
  border-radius: 10px;
  padding: 12px;
  text-align: center;
}

.solver-metric-label {
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--text-muted, #64748b);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 4px;
}

.solver-metric-value {
  font-size: 1.15rem;
  font-weight: 700;
  font-family: monospace;
}

.solver-metric-value.hard-ok {
  color: #16a34a;
}

.solver-metric-value.hard-ko {
  color: #dc2626;
}

.solver-metric-value.soft {
  color: #4f46e5;
}

.solver-timer-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: rgba(0, 0, 0, 0.04);
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 0.85rem;
  font-weight: 600;
  color: var(--text, #334155);
  margin-bottom: 18px;
}

.solver-note {
  font-size: 0.82rem;
  color: var(--text-muted, #64748b);
  line-height: 1.4;
  margin-bottom: 24px;
}

.btn-stop-solver {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  background: #fee2e2;
  color: #b91c1c;
  border: 1px solid #fecaca;
  padding: 10px 22px;
  border-radius: 25px;
  font-weight: 600;
  font-size: 0.92rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-stop-solver:hover:not(:disabled) {
  background: #fecaca;
  color: #991b1b;
  border-color: #fca5a5;
  box-shadow: 0 4px 10px rgba(220, 38, 38, 0.15);
}

.btn-stop-solver:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@keyframes solverPulse {
  0% { transform: scale(1); opacity: 0.8; }
  100% { transform: scale(1.6); opacity: 0; }
}

@keyframes solverFadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}
`;
  fs.writeFileSync(appCssPath, appCss, 'utf8');
  console.log('App.css updated.');
}

console.log('Updates 1 to 5 completed successfully.');
