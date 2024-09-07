package ru.barinov.file_browser

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun FileItem() {
    Card(
        border = BorderStroke(1.dp, Color.Gray),
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier
            .drawBehind {
//            drawLine()
            }
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            Image(
                painter = painterResource(id = ru.barinov.core.R.drawable.file),
                contentDescription = null,
                modifier = Modifier.size(44.dp)
            )
            Text(text = "fgfgfgf", modifier = Modifier.padding(start = 12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Text(text = "24.mb")
                Checkbox(checked = false, onCheckedChange = {})
            }

        }
    }
}


@Composable
fun FolderItem() {

}


//Previews
@Composable
@Preview(showBackground = true)
fun FileItemPreview() {
    FileItem()
}