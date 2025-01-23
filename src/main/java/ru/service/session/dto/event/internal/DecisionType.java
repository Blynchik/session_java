package ru.service.session.dto.event.internal;

import org.springframework.http.ResponseEntity;
import ru.service.session.client.HeroApi;
import ru.service.session.dto.hero.HeroResponse;

public enum DecisionType {
    STR {
        @Override
        public ResponseEntity<HeroResponse> increaseAttribute(String token, String userId, HeroApi heroApi) {
            return heroApi.increaseAttribute(token, Long.parseLong(userId), STR.name());
        }

        @Override
        public ResponseEntity<HeroResponse> decreaseAttribute(String token, String userId, HeroApi heroApi) {
            return heroApi.decreaseAttribute(token, Long.parseLong(userId), STR.name());
        }
    },
    DEX {
        @Override
        public ResponseEntity<HeroResponse> increaseAttribute(String token, String userId, HeroApi heroApi) {
            return heroApi.increaseAttribute(token, Long.parseLong(userId), DEX.name());
        }

        @Override
        public ResponseEntity<HeroResponse> decreaseAttribute(String token, String userId, HeroApi heroApi) {
            return heroApi.decreaseAttribute(token, Long.parseLong(userId), DEX.name());
        }
    },
    CON {
        @Override
        public ResponseEntity<HeroResponse> increaseAttribute(String token, String userId, HeroApi heroApi) {
            return heroApi.increaseAttribute(token, Long.parseLong(userId), CON.name());
        }

        @Override
        public ResponseEntity<HeroResponse> decreaseAttribute(String token, String userId, HeroApi heroApi) {
            return heroApi.decreaseAttribute(token, Long.parseLong(userId), CON.name());
        }
    },
    INT {
        @Override
        public ResponseEntity<HeroResponse> increaseAttribute(String token, String userId, HeroApi heroApi) {
            return heroApi.increaseAttribute(token, Long.parseLong(userId), INT.name());
        }

        @Override
        public ResponseEntity<HeroResponse> decreaseAttribute(String token, String userId, HeroApi heroApi) {
            return heroApi.decreaseAttribute(token, Long.parseLong(userId), INT.name());
        }
    },
    WIS {
        @Override
        public ResponseEntity<HeroResponse> increaseAttribute(String token, String userId, HeroApi heroApi) {
            return heroApi.increaseAttribute(token, Long.parseLong(userId), WIS.name());
        }

        @Override
        public ResponseEntity<HeroResponse> decreaseAttribute(String token, String userId, HeroApi heroApi) {
            return heroApi.decreaseAttribute(token, Long.parseLong(userId), WIS.name());
        }
    },
    CHA {
        @Override
        public ResponseEntity<HeroResponse> increaseAttribute(String token, String userId, HeroApi heroApi) {
            return heroApi.increaseAttribute(token, Long.parseLong(userId), CHA.name());
        }

        @Override
        public ResponseEntity<HeroResponse> decreaseAttribute(String token, String userId, HeroApi heroApi) {
            return heroApi.decreaseAttribute(token, Long.parseLong(userId), CHA.name());
        }
    },
    TEXT {
        @Override
        public ResponseEntity<HeroResponse> increaseAttribute(String token, String userId, HeroApi heroApi) {
            return null;
        }

        @Override
        public ResponseEntity<HeroResponse> decreaseAttribute(String token, String userId, HeroApi heroApi) {
            return null;
        }
    };

    public abstract ResponseEntity<HeroResponse> increaseAttribute(String token, String userId, HeroApi heroApi);

    public abstract ResponseEntity<HeroResponse> decreaseAttribute(String token, String userId, HeroApi heroApi);
}
