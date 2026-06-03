// Mirrors the backend QualitySummaryReportDto.
export interface QualitySummaryReport {
  total: number;
  passCount: number;
  warningCount: number;
  failCount: number;
  passRate: number;
  warningRate: number;
  failRate: number;
}
