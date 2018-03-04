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
    , children : Children
    }

type Children = Children (List Work)

children : Children -> List Work
children child =
    case child of
        Children c -> c

work : String -> List String -> Composers -> Artists -> Children -> Work
work = Work False False
