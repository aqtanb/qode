package com.qodein.feature.block.blocking

sealed interface BlockAction {
    data object ConfirmBlock : BlockAction
    data object CancelBlock : BlockAction
    data object DismissError : BlockAction
}
