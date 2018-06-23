FROM codesimple/elm:0.18

ADD src src
ADD public public
ADD elm-package.json elm-package.json

RUN elm make src/Main.elm --output public/main.js --yes