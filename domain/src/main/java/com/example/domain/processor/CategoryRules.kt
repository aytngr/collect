package com.example.domain.processor

import com.example.domain.models.Language
import com.example.domain.models.NoteCategory

object CategoryRules {
    val keywords: Map<Language, Map<NoteCategory, List<String>>> = mapOf(

        Language.ENGLISH to mapOf(
            NoteCategory.SHOPPING to listOf("buy", "get", "order", "groceries", "shopping", "store", "cart"),
            NoteCategory.FINANCE to listOf("pay", "bill", "invoice", "salary", "budget", "bank", "rent", "tax"),
            NoteCategory.WORK to listOf("meeting", "deadline", "project", "call", "email", "client", "report"),
            NoteCategory.SAVED to listOf("watch", "read", "movie", "book", "article", "video", "song", "recipe"),
        ),

        Language.TURKISH to mapOf(
            NoteCategory.SHOPPING to listOf("al", "satın al", "market", "alışveriş", "sipariş"),
            NoteCategory.FINANCE to listOf("öde", "fatura", "maaş", "kira", "banka", "para", "vergi"),
            NoteCategory.WORK to listOf("toplantı", "proje", "görev", "ara", "müşteri", "rapor", "son tarih"),
            NoteCategory.SAVED to listOf("izle", "oku", "film", "kitap", "makale", "video", "şarkı", "tarif"),
        ),

        Language.AZERBAIJANI to mapOf(
            NoteCategory.SHOPPING to listOf("al", "market", "mağaza", "bazar", "sifariş", "alış-veriş"),
            NoteCategory.FINANCE to listOf("ödə", "hesab", "maaş", "kirayə", "bank", "pul", "vergi"),
            NoteCategory.WORK to listOf("görüş", "layihə", "tapşırıq", "zəng", "müştəri", "hesabat"),
            NoteCategory.SAVED to listOf("bax", "oxu", "film", "kitab", "məqalə", "video", "mahnı", "resept"),
        ),
    )
}