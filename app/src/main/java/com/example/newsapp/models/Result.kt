package com.example.newsapp

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity (
    tableName = "results"
)
data class Result(
    @PrimaryKey(autoGenerate = true)
    val id:Int?= null,
    val ai_org: String,
    val ai_region: String,
    val ai_tag: String,
    val article_id: String,
    val content: String,
    val description: String,
    val duplicate: Boolean,
    val image_url: String,
    val language: String,
    val link: String,
    val pubDate: String,
    val sentiment: String,
    val sentiment_stats: String,
    val source_icon: String,
    val source_id: String,
    val source_name: String,
    val source_priority: Int,
    val source_url: String,
    val title: String,
):Serializable