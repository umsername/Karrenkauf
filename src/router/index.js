import { createRouter, createWebHistory } from 'vue-router'

import Home from '@/views/Home.vue'
import Impressum from '@/views/Impressum.vue'
import Sponsor from '@/views/Sponsor.vue'
import Profile from '@/views/Profile.vue'
import Terms from '@/views/Terms.vue'
import Privacy from '@/views/Privacy.vue'
import FAQ from '@/views/FAQ.vue'
import Contact from '@/views/Contact.vue'
import Help from '@/views/Help.vue'
import Feedback from '@/views/Feedback.vue'
import Credits from '@/views/Credits.vue'
import LegalNotice from '@/views/LegalNotice.vue'

// robots.txt can't be served through router – we simulate a view instead
import Robots from '@/views/Robots.vue'

export default createRouter({
    history: createWebHistory(),
    routes: [
        { path: '/', component: Home },

        { path: '/impressum', component: Impressum },
        { path: '/sponsor', component: Sponsor },
        { path: '/profile', component: Profile },
        { path: '/terms', component: Terms },
        { path: '/privacy', component: Privacy },
        { path: '/faq', component: FAQ },
        { path: '/contact', component: Contact },
        { path: '/help', component: Help },
        { path: '/feedback', component: Feedback },
        { path: '/credits', component: Credits },
        { path: '/legal-notice', component: LegalNotice },

        // simulated robots.txt via UI
        { path: '/robots.txt', component: Robots }
    ]
})
