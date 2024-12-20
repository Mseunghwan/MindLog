# 마음씨
감정 일기 분석 및 AI 상담 어플리케이션

## 소개
마음씨는 사용자의 일기를 기반으로 감정을 분석하고, AI와의 대화를 통해 정서적 지원을 제공하는 안드로이드 애플리케이션입니다.

## 주요 기능
- 📝 일기 작성
- 🔍 감정 분석: 일기의 내용을 분석하여 긍정/중립/부정 감정 비율 제공
- 📊 감정 통계: 최근 7일간의 감정 변화를 그래프로 시각화
- 💬 AI 상담: AI와의 대화를 통한 정서적 지원
- 📅 캘린더: 일기 작성 날짜 확인

## 기술 스택
- Kotlin
- Room Database
- Coroutines
- Flow
- Material Design
- MPAndroidChart
- Gemini AI API

## 설치 및 실행
1. 프로젝트를 클론합니다
2. com/example/letscouncil/GenerativeAiViewModelFactory.kt 파일에서 발급받은 Gemini API 키를 설정합니다:
   val GenerativeViewModelFactory = object : ViewModelProvider.Factory {
   private val apiKey = "APIKey"  // 실제 API 키로 교체하세요
