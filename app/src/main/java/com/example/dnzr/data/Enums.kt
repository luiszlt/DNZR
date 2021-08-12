package com.example.dnzfind.data

enum class Role {
    LEAD {
        override fun toString(): String {
            return "LEAD"
        }
    },
    FOLLOW {
        override fun toString(): String {
            return "FOLLOW"
        }
    },
    BOTH{
        override fun toString(): String {
            return "BOTH"
        }
    }
}