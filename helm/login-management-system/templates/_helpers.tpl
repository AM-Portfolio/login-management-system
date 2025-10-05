{{/*
Expand the name of the chart.
*/}}
{{- define "login-management-system.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
*/}}
{{- define "login-management-system.fullname" -}}
{{- default .Chart.Name .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create the name of the service account to use
*/}}
{{- define "login-management-system.serviceAccountName" -}}
{{- if .Values.serviceAccount.create }}
{{- default (include "login-management-system.fullname" .) .Values.serviceAccount.name }}
{{- else }}
{{- default "default" .Values.serviceAccount.name }}
{{- end }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "login-management-system.labels" -}}
helm.sh/chart: {{ include "login-management-system.chart" . }}
{{ include "login-management-system.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "login-management-system.selectorLabels" -}}
app.kubernetes.io/name: {{ include "login-management-system.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "login-management-system.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Infrastructure service names
*/}}
{{- define "login-management-system.postgresql.fullname" -}}
{{- .Values.postgresql.fullname }}
{{- end }}
