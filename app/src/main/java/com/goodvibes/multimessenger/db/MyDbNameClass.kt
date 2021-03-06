package com.example.testdb3.db

import android.provider.BaseColumns

object MyDbNameClass : BaseColumns {
    const val CHATS_TABLE_NAME = "chats"
    const val CHATS_TITLE_COLUMN_NAME = "title"
    const val CHATS_UID_COLUMN_NAME = "uid"

    const val FOLDERS_TABLE_NAME = "folders"
    const val FOLDERS_TITLE_COLUMN_NAME = "title"
    const val FOLDERS_UID_COLUMN_NAME = "uid"

    const val FOLDERS_SHARING_TABLE_NAME = "folders_sharing"
    const val FOLDERS_SHARING_FOLDER_ID_COLUMN_NAME = "folder_id"
    const val FOLDERS_SHARING_CHAT_ID_COLUMN_NAME = "chat_id"


    const val DATABASE_VERSION = 1
    const val DATABASE_NAME = "test3.db"

    const val CREATE_TABLE_CHATS = "CREATE TABLE IF NOT EXISTS $CHATS_TABLE_NAME (" +
            "${BaseColumns._ID} INTEGER PRIMARY KEY, " +
            "$CHATS_TITLE_COLUMN_NAME TEXT, " +
            "$CHATS_UID_COLUMN_NAME INTEGER, " +
            "UNIQUE ($CHATS_UID_COLUMN_NAME) ON CONFLICT IGNORE)"

    const val CREATE_TABLE_FOLDERS = "CREATE TABLE IF NOT EXISTS $FOLDERS_TABLE_NAME (" +
            "${BaseColumns._ID} INTEGER PRIMARY KEY, " +
            "$FOLDERS_TITLE_COLUMN_NAME TEXT, " +
            "$FOLDERS_UID_COLUMN_NAME INTEGER, " +
            "UNIQUE ($FOLDERS_UID_COLUMN_NAME) ON CONFLICT IGNORE)"

    const val CREATE_TABLE_FOLDERS_SHARING = "CREATE TABLE IF NOT EXISTS $FOLDERS_SHARING_TABLE_NAME (" +
            "$FOLDERS_SHARING_FOLDER_ID_COLUMN_NAME INTEGER, " +
            "$FOLDERS_SHARING_CHAT_ID_COLUMN_NAME INTEGER, " +
            "FOREIGN KEY($FOLDERS_SHARING_FOLDER_ID_COLUMN_NAME) REFERENCES $FOLDERS_TABLE_NAME($FOLDERS_UID_COLUMN_NAME), " +
            "FOREIGN KEY($FOLDERS_SHARING_CHAT_ID_COLUMN_NAME) REFERENCES $CHATS_TABLE_NAME($CHATS_UID_COLUMN_NAME), " +
            "PRIMARY KEY ($FOLDERS_SHARING_FOLDER_ID_COLUMN_NAME, $FOLDERS_SHARING_CHAT_ID_COLUMN_NAME))"

    const val DELETE_TABLE_CHATS = "DROP TABLE IF EXISTS ${CHATS_TABLE_NAME}"
    const val DELETE_TABLE_FOLDERS = "DROP TABLE IF EXISTS ${FOLDERS_TABLE_NAME}"
    const val DELETE_TABLE_FOLDERS_SHARING = "DROP TABLE IF EXISTS ${FOLDERS_SHARING_TABLE_NAME}"
}

object DBConst {
    const val RANDOM_START = 1000
    const val RANDOM_END = 1000000000
}
