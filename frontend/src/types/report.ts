// Mirrors the backend QualitySummaryReportDto.
export interface RunBreakdown {
  simulationRunId: number;
  total: number;
  passCount: number;
  warningCount: number;
  failCount: number;
}

export interface QualitySummaryReport {
  total: number;
  passCount: number;
  warningCount: number;
  failCount: number;
  passRate: number;
  warningRate: number;
  failRate: number;
  byRun: RunBreakdown[];
}
