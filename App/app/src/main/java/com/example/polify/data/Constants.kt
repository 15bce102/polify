package com.example.polify.data

const val ACTION_MATCH_FOUND = "com.example.polify.MATCH_FOUND"
const val ACTION_MATCH_RESULTS = "com.example.polify.MATCH_RESULTS"
const val ACTION_ROOM_INVITE = "com.example.polify.ROOM_INVITE"

const val EXTRA_BATTLE = "battle"
const val EXTRA_PLAYERS = "players"
const val EXTRA_BATTLE_SELECT = "select_battle"
const val EXTRA_BATTLE_ID = "battleId"
const val EXTRA_ROOM = "room"

const val QUE_TIME_LIMIT_MS = (15 * 1000).toLong()

const val KEY_TYPE = "type"
const val KEY_PAYLOAD = "payload"
const val KEY_BATTLE_ID = "battleId"

const val TYPE_MATCHMAKING = "matchmaking"
const val TYPE_SCORE_UPDATE = "score-update"
const val TYPE_ROOM_INVITE = "invite"

const val STATUS_OFFLINE = 0
const val STATUS_ONLINE = 1
const val STATUS_BUSY = 2
const val STATUS_WAITING = 3

const val BATTLE_ONE_VS_ONE = "1v1"
const val BATTLE_MULTIPLAYER = "Private room"
const val BATTLE_TEST = "test"