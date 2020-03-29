package com.example.a2ch.models.boards

import com.google.gson.annotations.SerializedName

data class BoardsBase(@SerializedName("Техника и софт")
                  val it: List<Category>?,
                      @SerializedName("Взрослым")
                  val adult: List<Category>?,
                      @SerializedName("Политика")
                  val politics: List<Category>?,
                      @SerializedName("Японская культура")
                  val japanese: List<Category>?,
                      @SerializedName("Разное")
                  val sundry: List<Category>?,
                      @SerializedName("Творчество")
                  val art: List<Category>?,
                      @SerializedName("Тематика")
                  val thematics: List<Category>?,
                      @SerializedName("Игры")
                  val games: List<Category>?,
                      @SerializedName("Пользовательские")
                  val userBoards: List<Category>?)