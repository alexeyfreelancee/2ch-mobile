package com.example.a2ch.models.boards

import com.google.gson.annotations.SerializedName

data class BoardsBase(@SerializedName("Техника и софт")
                  val it: List<Board>?,
                      @SerializedName("Взрослым")
                  val adult: List<Board>?,
                      @SerializedName("Политика")
                  val politics: List<Board>?,
                      @SerializedName("Японская культура")
                  val japanese: List<Board>?,
                      @SerializedName("Разное")
                  val sundry: List<Board>?,
                      @SerializedName("Творчество")
                  val art: List<Board>?,
                      @SerializedName("Тематика")
                  val thematics: List<Board>?,
                      @SerializedName("Игры")
                  val games: List<Board>?,
                      @SerializedName("Пользовательские")
                  val userBoards: List<Board>?)