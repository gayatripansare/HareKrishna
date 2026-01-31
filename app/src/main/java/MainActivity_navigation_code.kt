// Add this in your MainActivity.kt inside the bottom navigation listener

binding.bottomNavigation.setOnItemSelectedListener { item ->
    when (item.itemId) {
        R.id.nav_home -> {
            loadFragment(HomeFragment())
            true
        }
        R.id.nav_donate -> {
            loadFragment(DonateFragment())
            true
        }
        R.id.nav_kirtan -> {
            loadFragment(KirtanFragment())
            true
        }
        R.id.nav_japa -> {  // NEW JAPA BUTTON
            loadFragment(JapaFragment())
            true
        }
        R.id.nav_more -> {
            loadFragment(MoreFragment())
            true
        }
        else -> false
    }
}