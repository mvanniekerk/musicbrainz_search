module Work exposing (..)

type alias Composers = List String
type alias Artists = List String

type alias Work =
    { showMoreComposers : Bool
    , showMoreArtists : Bool
    , gid : String
    , name : List String
    , composer : Composers
    , artist : Artists
    }

work : String -> List String -> Composers -> Artists -> Work
work = Work False False
